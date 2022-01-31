package unit_tests
import kotlinx.coroutines.coroutineScope
import transaction_manager.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


suspend fun main(args: Array<String>) = coroutineScope {
    //val utxo_list1 = listOf(UTxO("0x000000001","198:99:30:1",50),UTxO("0x000000001","198:99:30:1",50))
    //val tr_list1 = listOf(Tr("198:99:30:1",50),Tr("198:99:30:2",25),Tr("198:99:30:2",25))
    //val tx1 = Tx("0x00000000150",utxo_list1,tr_list1)

    val utxo_list1 = listOf(UTxO("0x000000001","198:99:30:1",50),UTxO("0x000000001","198:99:30:1",25))
    val tr_list1 = listOf(Tr("198:99:30:1",50),Tr("198:99:30:2",25))
    val tx1 = Tx("0x00000000150",utxo_list1,tr_list1)

    val utxo_list2 = listOf(UTxO("0x00000000150","198:99:30:1",50))
    val tr_list2 = listOf(Tr("198:99:30:1",25),Tr("198:99:30:2",25))
    val tx2 = Tx("0x00000000151",utxo_list2,tr_list2)

    val tx_repo = TxRepo()
    println("client 198:99:30:1 utxo list: ${tx_repo.getClientUTXO("198:99:30:1")}")
    println("client 198:99:30:2 utxo list: ${tx_repo.getClientUTXO("198:99:30:2")}")

    println("commit status: ${tx_repo.commitTransaction(tx1)}")
    println("contain status: ${tx_repo.contains(tx1.tx_id)}")
    println("client 198:99:30:1 utxo list: ${tx_repo.getClientUTXO("198:99:30:1")}")
    println("client 198:99:30:2 utxo list: ${tx_repo.getClientUTXO("198:99:30:2")}")

    //println("un-commit status: ${tx_repo.unCommitTransaction(tx1)}")
    //println("contain status: ${tx_repo.contains(tx1.tx_id)}")
    //println("client 198:99:30:1 utxo list: ${tx_repo.getClientUTXO("198:99:30:1")}")
    //println("client 198:99:30:2 utxo list: ${tx_repo.getClientUTXO("198:99:30:2")}")
//
    //println("un-commit status: ${tx_repo.unCommitTransaction(tx1)}")
    //println("contain status: ${tx_repo.contains(tx1.tx_id)}")
    //println("client 198:99:30:1 utxo list: ${tx_repo.getClientUTXO("198:99:30:1")}")
    //println("client 198:99:30:2 utxo list: ${tx_repo.getClientUTXO("198:99:30:2")}")

    println("commit status: ${tx_repo.commitTransaction(tx2)}")
    println("contain status: ${tx_repo.contains(tx2.tx_id)}")
    println("client 198:99:30:1 utxo list: ${tx_repo.getClientUTXO("198:99:30:1")}")
    println("client 198:99:30:2 utxo list: ${tx_repo.getClientUTXO("198:99:30:2")}")

    println("un-commit status: ${tx_repo.unCommitTransaction(tx2)}")
    println("contain status: ${tx_repo.contains(tx2.tx_id)}")
    println("client 198:99:30:1 utxo list: ${tx_repo.getClientUTXO("198:99:30:1")}")
    println("client 198:99:30:2 utxo list: ${tx_repo.getClientUTXO("198:99:30:2")}")

    println("client 198:99:30:1 transaction history is: ${tx_repo.getClientTxHistory("198:99:30:1")}")
    println("client 198:99:30:2 transaction history is: ${tx_repo.getClientTxHistory("198:99:30:2")}")

    println("commit status: ${tx_repo.commitTransaction(tx2)}")
    println("contain status: ${tx_repo.contains(tx2.tx_id)}")
    println("client 198:99:30:1 utxo list: ${tx_repo.getClientUTXO("198:99:30:1")}")
    println("client 198:99:30:2 utxo list: ${tx_repo.getClientUTXO("198:99:30:2")}")

    println("client 198:99:30:1 transaction history is: ${tx_repo.getClientTxHistory("198:99:30:1")}")
    println("client 198:99:30:2 transaction history is: ${tx_repo.getClientTxHistory("198:99:30:2")}")


    val rooted_tr = RootedTr("198:99:30:1","198:99:30:2",25)
    val tx3 = Tx(rooted_tr)
    tx3.tx_id = "0x00000000152"

    println("commit status: ${tx_repo.commitTransaction(tx3)}")
    println("contain status: ${tx_repo.contains(tx3.tx_id)}")
    println("client 198:99:30:1 utxo list: ${tx_repo.getClientUTXO("198:99:30:1")}")
    println("client 198:99:30:2 utxo list: ${tx_repo.getClientUTXO("198:99:30:2")}")

    println("client 198:99:30:1 transaction history is: ${tx_repo.getClientTxHistory("198:99:30:1")}")
    println("client 198:99:30:2 transaction history is: ${tx_repo.getClientTxHistory("198:99:30:2")}")


    val rooted_tr4 = RootedTr("198:99:30:2","198:99:30:1",55)
    val tx4 = Tx(rooted_tr4)
    tx4.tx_id = "0x00000000153"

    println("commit status: ${tx_repo.commitTransaction(tx4)}")
    println("contain status: ${tx_repo.contains(tx4.tx_id)}")
    println("client 198:99:30:1 utxo list: ${tx_repo.getClientUTXO("198:99:30:1")}")
    println("client 198:99:30:2 utxo list: ${tx_repo.getClientUTXO("198:99:30:2")}")

    println("client 198:99:30:1 transaction history is: ${tx_repo.getClientTxHistory("198:99:30:1")}")
    println("client 198:99:30:2 transaction history is: ${tx_repo.getClientTxHistory("198:99:30:2")}")


    val rooted_tr5 = RootedTr("198:99:30:2","198:99:30:1",25)
    val tx5 = Tx(rooted_tr5)
    tx5.tx_id = "0x00000000154"

    try {
        println("commit status: ${tx_repo.commitTransaction(tx5)}")
    } catch (e: Exception) {
        println("Tx5 has failed! :(")
        println(e)
    }
    println("contain status: ${tx_repo.contains(tx5.tx_id)}")
    println("client 198:99:30:1 utxo list: ${tx_repo.getClientUTXO("198:99:30:1")}")
    println("client 198:99:30:2 utxo list: ${tx_repo.getClientUTXO("198:99:30:2")}")

    println("client 198:99:30:1 transaction history is: ${tx_repo.getClientTxHistory("198:99:30:1")}")
    println("client 198:99:30:2 transaction history is: ${tx_repo.getClientTxHistory("198:99:30:2")}")


    val rooted_tr6 = RootedTr("198:99:30:2","198:99:30:1",20)
    val tx6 = Tx(rooted_tr6)
    tx6.tx_id = "0x00000000155"

    println("commit status: ${tx_repo.commitTransaction(tx6)}")
    println("contain status: ${tx_repo.contains(tx6.tx_id)}")
    println("client 198:99:30:1 utxo list: ${tx_repo.getClientUTXO("198:99:30:1")}")
    println("client 198:99:30:2 utxo list: ${tx_repo.getClientUTXO("198:99:30:2")}")

    println("client 198:99:30:1 transaction history is: ${tx_repo.getClientTxHistory("198:99:30:1")}")
    println("client 198:99:30:2 transaction history is: ${tx_repo.getClientTxHistory("198:99:30:2")}")


    println("un-commit status: ${tx_repo.unCommitTransaction(tx6)}")
    println("contain status: ${tx_repo.contains(tx6.tx_id)}")
    println("client 198:99:30:1 utxo list: ${tx_repo.getClientUTXO("198:99:30:1")}")
    println("client 198:99:30:2 utxo list: ${tx_repo.getClientUTXO("198:99:30:2")}")

    println("client 198:99:30:1 transaction history is: ${tx_repo.getClientTxHistory("198:99:30:1")}")
    println("client 198:99:30:2 transaction history is: ${tx_repo.getClientTxHistory("198:99:30:2")}")


    println("commit status: ${tx_repo.commitTransaction(tx6)}")
    println("contain status: ${tx_repo.contains(tx6.tx_id)}")
    println("client 198:99:30:1 utxo list: ${tx_repo.getClientUTXO("198:99:30:1")}")
    println("client 198:99:30:2 utxo list: ${tx_repo.getClientUTXO("198:99:30:2")}")

    println("client 198:99:30:1 transaction history is: ${tx_repo.getClientTxHistory("198:99:30:1")}")
    println("client 198:99:30:2 transaction history is: ${tx_repo.getClientTxHistory("198:99:30:2")}")
}
