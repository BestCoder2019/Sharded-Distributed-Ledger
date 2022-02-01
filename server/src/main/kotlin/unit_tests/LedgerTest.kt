package unit_tests
import cs236351.txmanager.tx
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import transaction_manager.*
import zk_service.ZooKeeperKt
import zk_service.ZookeeperKtClient


suspend fun main(args: Array<String>) = coroutineScope {
    val id = args[0].toInt()

    val this_shard = LedgerService.getShard(id)
    val shard_id_list = listOf(0,1,2)
    val global_ledger_channels_id_list = listOf(8900,8901,8902,8910,8911,8912,8920,8921,8922)
    val shards_broadcast_channels_id_map = global_ledger_channels_id_list.associateWith { it + 1000 }
    val global_broadcast_channels_id_map = global_ledger_channels_id_list.associateWith { it + 200 }
    val shard_atomic_broadcast_id = shards_broadcast_channels_id_map[id]!!
    val global_atomic_broadcast_id = global_broadcast_channels_id_map[id]!!

    val shard_broadcast_channels = global_ledger_channels_id_list.filter { LedgerService.getShard(it) == this_shard }.associate {
        Pair(
            shards_broadcast_channels_id_map[it]!!,
            ManagedChannelBuilder.forAddress("localhost", shards_broadcast_channels_id_map[it]!!).usePlaintext().build()!!,
        )
    }
    val global_broadcast_channels = global_ledger_channels_id_list.associate {
        Pair(
            global_broadcast_channels_id_map[it]!!,
            ManagedChannelBuilder.forAddress("localhost", global_broadcast_channels_id_map[it]!!).usePlaintext().build()!!,
        )
    }
    val global_ledger_channels = global_ledger_channels_id_list.associateWith {
        ManagedChannelBuilder.forAddress("localhost", it).usePlaintext().build()!!
    }

    org.apache.log4j.BasicConfigurator.configure()

    val zk: ZooKeeperKt = ZookeeperKtClient()

    val ledger: LedgerService = LedgerService.make(
        this,
        zk,
        id,
        shard_id_list,
        shard_broadcast_channels,
        global_broadcast_channels,
        shard_atomic_broadcast_id,
        global_atomic_broadcast_id,
        global_ledger_channels,
    )

    // "Key press" barrier so only one propser sends messages
    withContext(Dispatchers.IO) { // Operations that block the current thread should be in a IO context
        System.`in`.read()
    }

    println("Start Test")

    val genesis_rooted_tr1 = RootedTr("0x00000000000000000000000000000000","198:99:30:1",50UL)
    val genesis_tx1 = Tx(genesis_rooted_tr1)
    checkTransaction(-1,genesis_tx1,ledger,id, listOf("0x00000000000000000000000000000000","198:99:30:1","198:99:30:2","198:99:30:3","191:99:30:1","191:99:30:2","192:99:30:1"))

    withContext(Dispatchers.IO) { // Operations that block the current thread should be in a IO context
        System.`in`.read()
    }

    val genesis_rooted_tr2 = RootedTr("0x00000000000000000000000000000000","198:99:30:1",25UL)
    val genesis_tx2 = Tx(genesis_rooted_tr2)
    checkTransaction(0,genesis_tx2,ledger,id, listOf("0x00000000000000000000000000000000","198:99:30:1","198:99:30:2","198:99:30:3","191:99:30:1","191:99:30:2","192:99:30:1"))

    withContext(Dispatchers.IO) { // Operations that block the current thread should be in a IO context
        System.`in`.read()
    }

    val utxo_list1 = listOf(UTxO(genesis_tx1.tx_id,"198:99:30:1",50UL),UTxO(genesis_tx2.tx_id,"198:99:30:1",25UL))
    val tr_list1 = listOf(Tr("198:99:30:1",50UL),Tr("198:99:30:2",25UL))
    val tx1 = Tx("0x00000000150",utxo_list1,tr_list1)
    checkTransaction(1,tx1,ledger,id, listOf("198:99:30:1","198:99:30:2","198:99:30:3","191:99:30:1","191:99:30:2","192:99:30:1"))

    withContext(Dispatchers.IO) { // Operations that block the current thread should be in a IO context
        System.`in`.read()
    }

    val utxo_list2 = listOf(UTxO(tx1.tx_id,"198:99:30:1",50UL))
    val tr_list2 = listOf(Tr("198:99:30:1",25UL),Tr("198:99:30:2",25UL))
    val tx2 = Tx("0x00000000151",utxo_list2,tr_list2)

    checkTransaction(2,tx2,ledger,id, listOf("198:99:30:1","198:99:30:2","198:99:30:3","191:99:30:1","191:99:30:2","192:99:30:1"))

    withContext(Dispatchers.IO) { // Operations that block the current thread should be in a IO context
        System.`in`.read()
    }

    // supposed to fail
    val utxo_list3 = listOf(UTxO(tx1.tx_id,"198:99:30:1",50UL))
    val tr_list3 = listOf(Tr("198:99:30:1",25UL),Tr("198:99:30:2",25UL))
    val tx3 = Tx("0x00000000152",utxo_list3,tr_list3)

    checkTransaction(3,tx3,ledger,id, listOf("198:99:30:1","198:99:30:2","198:99:30:3","191:99:30:1","191:99:30:2","192:99:30:1"))

    withContext(Dispatchers.IO) { // Operations that block the current thread should be in a IO context
        System.`in`.read()
    }

    val utxo_list4 = listOf(UTxO(tx1.tx_id,"198:99:30:2",25UL),UTxO(tx2.tx_id,"198:99:30:2",25UL))
    val tr_list4 = listOf(Tr("198:99:30:1",25UL),Tr("191:99:30:2",25UL))
    val tx4 = Tx("0x00000000153",utxo_list4,tr_list4)
    checkTransaction(4,tx4,ledger,id, listOf("198:99:30:1","198:99:30:2","198:99:30:3","191:99:30:1","191:99:30:2","192:99:30:1"))

    withContext(Dispatchers.IO) { // Operations that block the current thread should be in a IO context
        System.`in`.read()
    }

    val utxo_list5 = listOf(UTxO(tx4.tx_id,"191:99:30:2",25UL))
    val tr_list5 = listOf(Tr("192:99:30:1",10UL),Tr("191:99:30:1",15UL))
    val tx5 = Tx("0x00000000154",utxo_list5,tr_list5)
    checkTransaction(5,tx5,ledger,id, listOf("198:99:30:1","198:99:30:2","198:99:30:3","191:99:30:1","191:99:30:2","192:99:30:1"))

    withContext(Dispatchers.IO) { // Operations that block the current thread should be in a IO context
        System.`in`.read()
    }

    val utxo_list6 = listOf(UTxO(tx5.tx_id,"192:99:30:1",10UL))
    val tr_list6 = listOf(Tr("198:99:30:3",10UL))
    val tx6 = Tx("0x00000000155",utxo_list6,tr_list6)
    checkTransaction(6,tx6,ledger,id, listOf("198:99:30:1","198:99:30:2","198:99:30:3","191:99:30:1","191:99:30:2","192:99:30:1"))

    withContext(Dispatchers.IO) { // Operations that block the current thread should be in a IO context
        System.`in`.read()
    }

    val rooted_tr7 = RootedTr("198:99:30:3","191:99:30:2",5UL)
    val tx7 = Tx(rooted_tr7)
    checkTransaction(7,tx7,ledger,id, listOf("198:99:30:1","198:99:30:2","198:99:30:3","191:99:30:1","191:99:30:2","192:99:30:1"))

    withContext(Dispatchers.IO) { // Operations that block the current thread should be in a IO context
        System.`in`.read()
    }

    val rooted_tr8 = RootedTr("198:99:30:1","192:99:30:1",50UL)
    val tx8 = Tx(rooted_tr8)
    checkTransaction(8,tx8,ledger,id, listOf("198:99:30:1","198:99:30:2","198:99:30:3","191:99:30:1","191:99:30:2","192:99:30:1"))

    withContext(Dispatchers.IO) { // Operations that block the current thread should be in a IO context
        System.`in`.read()
    }

    val rooted_tr9 = RootedTr("198:99:30:2","198:99:30:1",1UL)
    val tx9 = Tx(rooted_tr9)
    checkTransaction(9,tx9,ledger,id, listOf("198:99:30:1","198:99:30:2","198:99:30:3","191:99:30:1","191:99:30:2","192:99:30:1"))

    withContext(Dispatchers.IO) { // Operations that block the current thread should be in a IO context
        System.`in`.read()
    }

    ledger.close()
}

suspend fun checkTransaction(tx_id: Int, tx: Tx, ledger: LedgerService, id: ID, clients_to_check_list: List<Address>) {
    try {
        println("tx$tx_id commit status for id=$id is ${ledger.process(tx)}")
    } catch (e: Exception) {
        println("tx$tx_id has failed! :(")
        println(e)
    }

    for(client in clients_to_check_list) {
        println("ledger Tx history for client=$client is ${ledger.getClientTxHistory(client)}")
    }
    for(client in clients_to_check_list) {
        println("ledger UTxOs for client=$client is ${ledger.getClientUTXO(client)}")
    }
}