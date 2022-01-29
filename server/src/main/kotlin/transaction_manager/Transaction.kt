package transaction_manager

import kotlinx.serialization.Serializable

typealias TxID = String
typealias Address = String

@Serializable
data class UTxO(
    public val tx_id: TxID,
    public val address: Address,
    public val coins: Long,
) { }

@Serializable
data class Tr(
    public val address: Address,
    public val coins: Long,
) { }

@Serializable
data class Tx(
    public val tx_id: TxID,
    public val inputs: List<UTxO>,
    public val outputs: List<Tr>,
    public var timestamp: String = "",
) {
    public fun isLegal(): Boolean {
        val expected_sender_addr: Address = inputs[0].address
        return (!inputs.any { it.address != expected_sender_addr }) &&
                (inputs.sumOf { it.coins } == outputs.sumOf { it.coins }) &&
                (!inputs.any { it.tx_id == tx_id })
    }

    public fun getSenderAddress(): Address {
        assert(this.isLegal())
        return inputs[0].address
    }

    public fun getReceiverAddressList(): List<Address> {
        assert(this.isLegal())
        return outputs.map { it.address }
    }
}

@Serializable
data class TxList(
    public val tx_list: List<Tx>,
) {
    public fun isLegal(): Boolean {
        /*
        val transactions_utxo_txid: List<Set<TxID>> = tx_list.map { it.inputs.map { it.tx_id }.toSet() }
        val inputs_txid: Set<TxID> = transactions_utxo_txid.fold(mutableSetOf()) { acc: Set<TxID>, set: Set<TxID> -> acc + set }
        val transactions_txid: Set<TxID> = tx_list.map { it.tx_id }.toSet()
        return (inputs_txid intersect transactions_txid).isEmpty() && (!tx_list.any { !it.isLegal() })
         */ //TODO: Consider optimization
        return (!tx_list.any { (it.inputs.map { it.tx_id }.toSet() intersect (tx_list.map { it.tx_id }).toSet()).isNotEmpty() })
                && (!tx_list.any { !it.isLegal() })
    }
}