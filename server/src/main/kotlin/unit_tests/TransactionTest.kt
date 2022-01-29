package unit_tests
import kotlinx.coroutines.coroutineScope
import transaction_manager.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


suspend fun main(args: Array<String>) = coroutineScope {
    val utxo_list1 = listOf(UTxO("0x000000001","198:99:30:1",50),UTxO("0x000000001","198:99:30:1",50))
    val tr_list1 = listOf(Tr("198:99:30:1",50),Tr("198:99:30:2",25),Tr("198:99:30:2",25))
    val Tx1 = Tx("0x00000000150",utxo_list1,tr_list1)

    println(Tx1.isLegal())

    val Tx1_json = Json.encodeToString(Tx1)
    println(Tx1_json)
    val Tx1_restored = Json.decodeFromString<Tx>(Tx1_json)
    println(Tx1_restored == Tx1)

    val list = listOf<Tx>(Tx1)
    val tx_list = TxList(list)
    val Tx1_list_json = Json.encodeToString(tx_list)
    println(Tx1_list_json)
    val Tx1_list_restored = Json.decodeFromString<TxList>(Tx1_list_json)
    println(Tx1_list_restored == tx_list)

    println(Tx1.getSenderAddress())
    println(Tx1.getReceiverAddressList())
}
