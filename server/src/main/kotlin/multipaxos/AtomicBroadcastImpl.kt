package multipaxos

import com.google.protobuf.ByteString
import cs236351.multipaxos.Ack
import cs236351.multipaxos.AckMessage
import cs236351.multipaxos.AtomicBroadcastSequencerServiceGrpcKt.AtomicBroadcastSequencerServiceCoroutineStub as SequencerGrpcStub
import cs236351.multipaxos.message
import io.grpc.ManagedChannel
import io.grpc.ServerBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit

class AtomicBroadcastImpl(
    private val scope: CoroutineScope,
    private val id: ID,
    channels: Map<ID, ManagedChannel>,
    biSerializer: ByteStringBiSerializer<String>,
    private val omega: OmegaFailureDetector<Int>,
    receiveCapacityBufferSize: Int = 100,
) : AtomicBroadcast<String>(LearnerService(scope),biSerializer,receiveCapacityBufferSize) {
    val acceptor = AcceptorService(id)
    val this_sequencer: Proposer = Proposer(
        id = id, omegaFD = omega, scope = scope, acceptors = channels,
        thisLearner = learner, proposalCapacityBufferSize = Channel.UNLIMITED
    )
    val sequencer_service = SequencerService(this_sequencer,scope)
    private val sequencers: Map<ID, SequencerGrpcStub> = (channels.mapValues { (_, v) -> SequencerGrpcStub(v) })
    private val sequencer_server = ServerBuilder.forPort(id)
        .apply {
            addService(sequencer_service)
        }
        .apply {
            if (id > 0) // Apply your own logic: who should be an acceptor
                addService(acceptor)
        }
        .apply {
            if (id > 0) // Apply your own logic: who should be a learner
                addService(learner)
        }
        .build()

    private val broadcastStream = Channel<ByteString>(Channel.UNLIMITED)
    private val broadcastSendStream: SendChannel<ByteString> = broadcastStream

    private val sequencerSender = object {
        private var deliver_sync_chan = Channel<Unit>(1)
        private var send_sync_chan = Channel<Unit>(1)
        var leader_cache = omega.leader
        var sent_message: ByteString? = null
        var is_message_delivered: Boolean = true
        var is_resending: Boolean = false
        var last_delivered_number: Long = 0
        private val prev_leader_update_mutex = Mutex()

        init {
            omega.addWatcher {
                if(leader_cache != omega.leader) {
                    var prev_leader_cache: Int
                    prev_leader_update_mutex.withLock {
                        leader_cache = omega.leader
                        prev_leader_cache = leader_cache
                    }
                    val prev_last_delivered_number: Long = last_delivered_number
                    delay(1000)
                    //println("last delivered $last_delivered_number, pev last dilvered = $prev_last_delivered_number," +
                    //        " id = $id, prev_leader = $prev_leader_cache, current_leader = $leader_cache, " +
                    //        "is delivered = $is_message_delivered")
                    if(!is_message_delivered
                        && (prev_last_delivered_number == last_delivered_number)
                        && (prev_leader_cache == leader_cache)) {
                        send_sync_chan.receive()
                        deliver_sync_chan.send(Unit)
                        is_resending = true
                        //println("resending")
                    }
                }
            }
        }

        suspend fun sendToSequencer(byteString: ByteString) {
            assert(!byteString.isEmpty)

            do {
                //println("sending to seq, message number: ${last_delivered_number + 1}")
                try{
                    val result: AckMessage = sequencers[leader_cache]!!.withDeadlineAfter(5, TimeUnit.SECONDS).broadcastMessage(
                        message {
                            this.value = byteString
                        }
                    )
                    assert(result.ack == Ack.YES)
                } catch (e: Exception) {
                    println("AtomicBroadcast: Failed to send message to the Sequencer. Sequencer id=$leader_cache")
                    println(e)
                }

                sent_message = byteString
                assert(is_message_delivered || is_resending)
                is_message_delivered = false
                send_sync_chan.send(Unit)

                deliver_sync_chan.receive()
            } while(!is_message_delivered)
        }

        fun latestIsDelivered(byteString: ByteString) = scope.launch {
            send_sync_chan.receive()

            assert(sent_message!! == byteString)
            assert(!is_message_delivered)

            is_message_delivered = true
            last_delivered_number++
            is_resending = false

            deliver_sync_chan.send(Unit)
        }
    }

    init {
        learner.learnerChannels = channels.filterKeys { it != id }

        this_sequencer.start()
        startSending()
        sequencer_server.start()
    }

    private fun startSending() = scope.launch {
        for(broadcast_message in broadcastStream){
            sequencerSender.sendToSequencer(broadcast_message)
        }
    }

    override suspend fun _send(byteString: ByteString) {
        val message: ByteString = biSerializer("@$id:").concat(byteString)
        broadcastSendStream.send(message)
    }

    override fun _deliver(byteString: ByteString): List<String> {
        val message: String = biSerializer(byteString)
        if(message.startsWith("@$id:")) {
            sequencerSender.latestIsDelivered(byteString)
        }
        val original_message = message.substringAfter(':')
        assert(original_message != message)
        return listOf(original_message)
    }

    fun close() {
        sequencer_server.awaitTermination()
    }
}
