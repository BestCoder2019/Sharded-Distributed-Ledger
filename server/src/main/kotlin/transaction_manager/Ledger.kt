package transaction_manager

import com.example.api.exception.BadTransactionException
import com.google.protobuf.Empty
import com.google.protobuf.empty
import cs236351.txmanager.*
import cs236351.txmanager.LedgerServiceGrpcKt.LedgerServiceCoroutineImplBase as LedgerImplBase
import cs236351.txmanager.LedgerServiceGrpcKt.LedgerServiceCoroutineStub as LedgerGrpcStub
import io.grpc.ManagedChannel
import io.grpc.ServerBuilder
import io.grpc.StatusException
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import multipaxos.AtomicBroadcastImpl
import multipaxos.ZooKeeperOmegaFailureDetector
import multipaxos.biSerializer
import org.springframework.http.HttpStatus
import zk_service.ZooKeeperKt
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

enum class DispatchAction {
    Notify {
        override suspend operator fun invoke(stub: LedgerGrpcStub, seconds_deadline: Long, tx: cs236351.txmanager.Tx) =
            stub.withDeadlineAfter(seconds_deadline, TimeUnit.SECONDS).notify(tx)
           },
    Submit {
        override suspend operator fun invoke(stub: LedgerGrpcStub, seconds_deadline: Long, tx: cs236351.txmanager.Tx) =
            stub.withDeadlineAfter(seconds_deadline, TimeUnit.SECONDS).submit(tx)
           };

    abstract suspend operator fun invoke(stub: LedgerGrpcStub, seconds_deadline: Long, tx: cs236351.txmanager.Tx): Any?
}

typealias ID = Int
typealias ShardID = Int

const val NoRepresentative: ID = -1

class LedgerService private constructor(
    private val scope: CoroutineScope,
    id: ID,
    shard_ids: List<ShardID>,
    private val shard_atomic_broadcast: AtomicBroadcastImpl,
    private val global_atomic_broadcast: AtomicBroadcastImpl,
    global_ledger_channels: Map<ID, ManagedChannel>,
    private val shard_memberships: Map<ShardID,Membership>,
    context: CoroutineContext = txManagerThread,
) : LedgerImplBase(context) {

    private val tx_commit_status_deferreds: ConcurrentMap<TxID, CompletableDeferred<Tx>> = ConcurrentHashMap()
    private val tx_commit_sync_channel_mutex: Mutex = Mutex()
    private val shard_tx_repo: TxRepo = TxRepo()
    private val global_tx_repo: TxRepo = TxRepo()
    private var shard_representatives: ConcurrentMap<ShardID, ID> = ConcurrentHashMap()
    private val on_changes: Map<ShardID, (suspend () -> Unit)> = shard_ids.associateWith {
        {
            try {
                this.shard_representatives[it] = shard_memberships[it]!!.queryMembers().random().toInt()
                println("Membership on change: id=$id picked representative=${this.shard_representatives[it]} for shard=$it")
            } catch (_: Exception) {
                println("Membership on change failed with shard_id=$it. Assigning a no shard representative.")
                this.shard_representatives[it] = NoRepresentative
            }
        }
    }
    private val this_shard: ShardID = getShard(id)

    private val ledgers: Map<ID, LedgerGrpcStub> = (global_ledger_channels.mapValues { (_, v) -> LedgerGrpcStub(v) })
    private val ledger_server = ServerBuilder.forPort(id)
        .apply {
            addService(this@LedgerService)
        }
        .build()

    private fun populateTxID(tx: Tx) {
        do {
            tx.populateID()
            val cache_result: Tx? = global_tx_repo.getTx(tx.tx_id)
        } while(((cache_result != null) && (cache_result != tx)) ||
            tx.inputs.any { it.tx_id == tx.tx_id })
    }

    init {
        shard_memberships.map {
            it.value.onChange = on_changes[it.key]!!
        }

        ledger_server.start()
        start()
    }

    private val txDispatcher = object {

        val shard_drawing_lock: Mutex = Mutex()

        suspend fun contactShard(shard_id: ShardID, request: cs236351.txmanager.Tx,
                                 rpc: DispatchAction): Any {
            var is_sent = false
            var result: Any? = ackMessage {
                this.ack = Ack.NO
            }
            var shard_representative: ID
            while(!is_sent) {
                shard_drawing_lock.lock()
                while(shard_representatives[shard_id] == NoRepresentative) {
                    println("Representative for sender_shard=$shard_id is NoRepresentative. Redrawing representative.")
                    shard_representatives[shard_id] = try {
                        shard_memberships[shard_id]!!.queryMembers().random().toInt()
                    } catch (e: Exception) {
                        println("Failed to draw representative. Waiting for 1 second and trying again.")
                        NoRepresentative
                    }
                    delay(1000)
                }
                shard_representative = shard_representatives[shard_id]!!
                shard_drawing_lock.unlock()

                try {
                    if(shard_representative == NoRepresentative) continue
                    result = rpc(ledgers[shard_representative]!!,4,request)
                    is_sent = true
                } catch (e: Exception) {
                    println("Failed to submit tx=$request via gRPC, representative is " +
                            "${shard_representative}. Redrawing representative.")
                    println(e)
                    shard_drawing_lock.lock()
                    if(shard_representatives[shard_id] == shard_representative) {
                        shard_representatives[shard_id] = try {
                            shard_memberships[shard_id]!!.queryMembers().random().toInt()
                        } catch (e: Exception) {
                            println("Failed to draw representative. Waiting for 1 second and trying again.")
                            delay(1000)
                            NoRepresentative
                        }
                    }
                    shard_drawing_lock.unlock()
                }
            }
            return result!!
        }

        fun notifyShards(remote_receiver_shards: List<ShardID>, request: cs236351.txmanager.Tx) {
            scope.launch(context) {
                remote_receiver_shards.map {
                    async(context) {
                        try {
                            contactShard(it,request,DispatchAction.Notify)
                        } catch (e: StatusException) {
                            println("Failed to notify shard=$it.")
                            println(e)
                        }
                    }
                }.awaitAll()
            }
        }

        suspend fun submitToShard(sender_shard: ShardID, request: cs236351.txmanager.Tx): cs236351.txmanager.Tx {
            return contactShard(sender_shard,request,DispatchAction.Submit) as cs236351.txmanager.Tx
        }

    }

    private fun start() = scope.launch(context) {
        for ((`seq#`, tx_encoding) in shard_atomic_broadcast.stream) {
            println("Message #$`seq#`: $tx_encoding  received!")
            val tx: Tx = Json.decodeFromString(tx_encoding)

            if(shard_tx_repo.contains(tx.tx_id)) {
                if(tx_commit_status_deferreds.contains(tx.tx_id)) {
                    tx_commit_status_deferreds[tx.tx_id]!!.complete(
                        try {
                        shard_tx_repo.getTx(tx.tx_id)!!
                        } catch (e: Exception) {
                            println("Internal Error: Tx not found in local shard although it is contained in it!!")
                            println(e)
                            Tx()
                        })
                continue
                }
            }

            val receiver_addresses: List<Address> = tx.getReceiverAddressList()
            assert((getClientShard(tx.getSenderAddress()) == this_shard) ||
                    receiver_addresses.map { getClientShard(it) }.contains(this_shard))

            try {
                shard_tx_repo.commitTransaction(tx)
                global_tx_repo.commitTransaction(tx)
            } catch (e: Exception) {
                println("Transaction $tx was not committed")
                println("un-commit status from global_tx_repo: ${global_tx_repo.unCommitTransaction(tx)}")
                println("un-commit status from shard_tx_repo: ${shard_tx_repo.unCommitTransaction(tx)}")
                println(e)
                if(tx_commit_status_deferreds.contains(tx.tx_id)) {
                    tx_commit_status_deferreds[tx.tx_id]!!.complete(Tx())
                }
                continue
            }
            tx_commit_status_deferreds[tx.tx_id]?.complete(tx)
        }
    }

    override suspend fun submit(request: cs236351.txmanager.Tx): cs236351.txmanager.Tx {
        val tx = Tx(request)
        val receiver_addresses: List<Address> = tx.getReceiverAddressList()
        assert(getClientShard(tx.getSenderAddress()) == this_shard)

        println("got to mutex lock")
        tx_commit_sync_channel_mutex.lock()
        if(shard_tx_repo.contains(tx.tx_id)) {
            tx_commit_sync_channel_mutex.unlock()
            return try {
                shard_tx_repo.getTx(tx.tx_id)!!.getProtoBufferTx()
            } catch (e: Exception) {
                println("Internal Error: Tx not found in local shard although it is contained in it!!")
                println(e)
                Tx().getProtoBufferTx()
            }
        }
        if(tx_commit_status_deferreds.contains(tx.tx_id)) {
            tx_commit_sync_channel_mutex.unlock()
            return try {
                tx_commit_status_deferreds[tx.tx_id]!!.await().getProtoBufferTx()
            } catch (e: Exception) {
                println("Tx commit status deferred failed!")
                println(e)
                Tx().getProtoBufferTx()
            }
        }
        tx_commit_status_deferreds[tx.tx_id] = CompletableDeferred()
        tx_commit_sync_channel_mutex.unlock()

        println("tx ${tx.tx_id}sent to atomic broadcast:")
        shard_atomic_broadcast.send(Json.encodeToString(tx))
        val response: Tx = try {
            println("waiting for answer:")
            tx_commit_status_deferreds[tx.tx_id]!!.await()
        } catch (e: Exception) {
            println("Tx commit status deferred failed!")
            println(e)
            Tx()
        }
        assert(response.tx_type != TxType.TransferBased)
        if(response.tx_id.isNotEmpty()) {
            val remote_receiver_shards = receiver_addresses.map { getClientShard(it) }.filter { it != this_shard }.distinct()
            txDispatcher.notifyShards(remote_receiver_shards,response.getProtoBufferTx())
        }
        return response.getProtoBufferTx()
    }

    override suspend fun notify(request: cs236351.txmanager.Tx): Empty {
        assert(request.txType != TxType.TransferBased)
        val tx = Tx(request)
        assert(tx.tx_id.isNotEmpty())
        val sender_address: Address = tx.getSenderAddress()
        val reciever_shards: List<ShardID> = tx.getReceiverAddressList().map { getClientShard(it) }
        assert(reciever_shards.contains(this_shard) &&
            (getClientShard(sender_address) != this_shard))

        val response: Tx
        tx_commit_sync_channel_mutex.lock()
        if(shard_tx_repo.contains(tx.tx_id)) {
            tx_commit_sync_channel_mutex.unlock()
            response = try {
                shard_tx_repo.getTx(tx.tx_id)!!
            } catch (e: Exception) {
                println("Internal Error: Tx not found in local shard although it is contained in it!!")
                println(e)
                Tx()
            }
            assert(response.tx_id.isNotEmpty())
            return empty { }
        }
        if(tx_commit_status_deferreds.contains(tx.tx_id)) {
            tx_commit_sync_channel_mutex.unlock()
            response = try {
                tx_commit_status_deferreds[tx.tx_id]!!.await()
            } catch (e: Exception) {
                println("Tx commit status deferred failed!")
                println(e)
                Tx()
            }
            assert(response.tx_id.isNotEmpty())
            return empty { }
        }
        tx_commit_status_deferreds[tx.tx_id] = CompletableDeferred()
        tx_commit_sync_channel_mutex.unlock()

        println("tx ${tx.tx_id}sent to atomic broadcast:")
        shard_atomic_broadcast.send(Json.encodeToString(tx))
        response = try {
            println("waiting for answer:")
            tx_commit_status_deferreds[tx.tx_id]!!.await()
        } catch (e: Exception) {
            println("Tx commit status deferred failed!")
            println(e)
            Tx()
        }
        assert(response.tx_id.isNotEmpty())

        return empty { }
    }

    /**
     * Throws: BadTransactionException in-case the transaction wasn't committed for any reason.
     *
     */
    public suspend fun process(request: Tx): Tx {
        println("Received Request: ${request}")
        populateTxID(request)
        if(!request.isLegal()) throw BadTransactionException(HttpStatus.BAD_REQUEST,"Tx is illegal!")

        val sender_shard = getClientShard(request.getSenderAddress())
        val receiver_shards: List<ShardID> = request.getReceiverAddressList().map { getClientShard(it) }
        val response: Tx

        println("Submitting Request: ${request}")
        if(sender_shard == this_shard) {
            response = Tx(submit(request.getProtoBufferTx()))
        } else {
            response = Tx(txDispatcher.submitToShard(sender_shard, request.getProtoBufferTx()))
            if(response.tx_id.isNotEmpty()) {
                try {
                    global_tx_repo.commitTransaction(response)
                    if(receiver_shards.contains(this_shard)) {
                        shard_tx_repo.commitTransaction(response)
                    }
                } catch(e: Exception) {
                    println("failed to commit transaction after it was submitted and committed at a remote shard!")
                    println("un-commit status from global_tx_repo: ${global_tx_repo.unCommitTransaction(response)}")
                    if(receiver_shards.contains(this_shard)) {
                        println("un-commit status from shard_tx_repo: ${shard_tx_repo.unCommitTransaction(response)}")
                    }
                    println(e)
                    throw BadTransactionException(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error occurred while" +
                            " committing the transaction local, after it was accepted at the responsible remote shard!")
                }
            }
        }
        println("Got response: ${response}")
        if(response.tx_id.isNotEmpty()) {
            return try {
                getTx(response.tx_id)!!
            } catch (e: Exception) {
                println(e)
                throw BadTransactionException(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Error: Transaction was " +
                        "committed locally but could not be found in the repo!")
            }
        }
        throw BadTransactionException(HttpStatus.BAD_REQUEST,"Transaction was rejected!")
    }

    public fun getClientUTXO(address: Address): List<UTxO> {
        return global_tx_repo.getClientUTXO(address)
    }

    public fun getTx(tx_id: TxID): Tx? {
        return global_tx_repo.getTx(tx_id)
    }

    public fun getClientTxHistory(address: Address, first_n: Int = -1): TxList {
        return global_tx_repo.getClientTxHistory(address,first_n)
    }

    public fun close() {
        ledger_server.awaitTermination()
        shard_atomic_broadcast.close()
        global_atomic_broadcast.close()
    }

    companion object {
        suspend fun make(
            scope: CoroutineScope,
            zk: ZooKeeperKt,
            id: ID,
            shard_ids: List<ShardID>,
            shard_broadcast_channels: Map<ID, ManagedChannel>,
            global_broadcast_channels: Map<ID, ManagedChannel>,
            shard_atomic_broadcast_id: ID,
            global_atomic_broadcast_id: ID,
            global_ledger_channels: Map<ID, ManagedChannel>,
            context: CoroutineContext = txManagerThread,
        ): LedgerService {
            val shard_atomic_broadcast_omega = ZooKeeperOmegaFailureDetector.make(shard_atomic_broadcast_id,zk,
                "shards_broadcast/shard#${getShard(id)}")
            val shard_atomic_broadcast = AtomicBroadcastImpl(
                scope,
                shard_atomic_broadcast_id,
                shard_broadcast_channels,
                biSerializer,
                shard_atomic_broadcast_omega,
            )

            val global_atomic_broadcast_omega = ZooKeeperOmegaFailureDetector.make(global_atomic_broadcast_id,zk,
                "global_broadcast")
            val global_atomic_broadcast = AtomicBroadcastImpl(
                scope,
                global_atomic_broadcast_id,
                global_broadcast_channels,
                biSerializer,
                global_atomic_broadcast_omega,
            )

            val shard_memberships: Map<ShardID,Membership> = shard_ids.associateWith {
                Membership.make(zk,"shard#$it")
            }
            shard_memberships[getShard(id)]!!.join(id.toString())

            val ledger = LedgerService(scope, id, shard_ids, shard_atomic_broadcast, global_atomic_broadcast,
                global_ledger_channels, shard_memberships, context)

            ledger.shard_representatives = ConcurrentHashMap(shard_ids.associateWith {
                try {
                    shard_memberships[it]!!.queryMembers().random().toInt()
                } catch (e: Exception) {
                    println(e)
                    NoRepresentative
                }
            })

            return ledger
        }

        fun getShard(id: ID): ShardID {
            if(id in 8900..8909) {
                return 0
            } else if(id in 8910..8919) {
                return 1
            } else if(id in 8920..8929) {
                return 2
            }
            return 0
        }

        fun getClientShard(address: Address): ShardID {
            if(address.startsWith("190:99:")) {
                return 0
            } else if(address.startsWith("191:99:")) {
                return 1
            } else if(address.startsWith("192:99:")) {
                return 2
            }
            return 0
        }
    }
}