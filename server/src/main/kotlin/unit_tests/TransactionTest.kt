package unit_tests
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.coroutineScope
import transaction_manager.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


suspend fun main(args: Array<String>) = coroutineScope {
    val utxo_list1 = listOf(UTxO("0x000000001","198:99:30:1",50),UTxO("0x000000001","198:99:30:1",50))
    val tr_list1 = listOf(Tr("198:99:30:1",50),Tr("198:99:30:2",25),Tr("198:99:30:2",25))
    val Tx1 = Tx("0x00000000150",utxo_list1,tr_list1)

    testTx(Tx1, 1)

    val rooted_tr2 = RootedTr("198:99:30:1", "198:99:30:2", 50)

    testTx(Tx(rooted_tr2),2)

}

fun testTx(Tx1: Tx, id: Int) {
    println("********** Tx${id} **********")
    println("Is Tx${id} Legal: ${Tx1.isLegal()}")

    println("Tx Json Conversion Test:")
    println(ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(Tx1))
    println(Tx1.toString())
    val Tx1_json = Json.encodeToString(Tx1)
    println(Tx1_json)
    val Tx1_restored = Json.decodeFromString<Tx>(Tx1_json)
    println(Tx1_restored == Tx1)

    //val list = listOf<Tx>(Tx1)
    //val tx_list = TxList(list)
    //val Tx1_list_json = Json.encodeToString(tx_list)
    //println(Tx1_list_json)
    //val Tx1_list_restored = Json.decodeFromString<TxList>(Tx1_list_json)
    //println(Tx1_list_restored == tx_list)

    println("Tx${id} Sender Address: ${Tx1.getSenderAddress()}")
    println("Tx${id} Receiver Address List: ${Tx1.getReceiverAddressList()}")

    println("TxID generation test:")
    Tx1.populateID()
    println(Tx1.toString())
    Tx1.populateID()
    println(Tx1.toString())
    Tx1.populateID()
    println(Tx1.toString())

    println("ProtoBuff conversion test:")
    val protobuff_tx: cs236351.txmanager.Tx = Tx1.getProtoBufferTx()
    println(protobuff_tx)
    println(Tx1)
    println(Tx(protobuff_tx))
    println(Tx(protobuff_tx) == Tx1)
}
