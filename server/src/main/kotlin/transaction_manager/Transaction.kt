package transaction_manager

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonAutoDetect
import cs236351.txmanager.*
import kotlinx.serialization.Serializable
import com.google.common.hash.HashCode
import com.google.common.hash.HashFunction
import com.google.common.hash.Hashing
import java.util.*

typealias TxID = String
typealias Address = String

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Serializable
data class UTxO(
    public val tx_id: TxID,
    public val address: Address,
    public val coins: Long,
) { }

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Serializable
data class Tr(
    public val address: Address,
    public val coins: Long,
) { }

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Serializable
data class RootedTr(
    public val source: Address,
    public val destination: Address,
    public val coins: Long,
) { }

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Serializable
data class Tx(
    public var tx_id: TxID = "",
    public var inputs: List<UTxO>,
    public var outputs: List<Tr>,
    @JsonIgnore
    public var timestamp: String = "",
    @JsonIgnore
    public val rooted_tr: RootedTr? = null,
    @JsonIgnore
    public var tx_type: TxType = TxType.Regular,
) {
    @JsonIgnore
    constructor() : this(
        inputs=emptyList(),
        outputs=emptyList())

    @JsonIgnore
    constructor(rooted_tr: RootedTr) : this(
        inputs=emptyList(),
        outputs=emptyList(),
        rooted_tr=rooted_tr,
        tx_type=TxType.TransferBased)

    @JsonIgnore
    constructor(protobuff_tx: cs236351.txmanager.Tx) : this(
        tx_id=protobuff_tx.txId,
        inputs=protobuff_tx.inputsList.map { UTxO(it.txId, it.address, it.coins) },
        outputs=protobuff_tx.outputsList.map { Tr(it.address, it.coins) },
        timestamp=protobuff_tx.timestamp,
        rooted_tr=if(protobuff_tx.rootedTr.source.isNotEmpty()) {
            RootedTr(protobuff_tx.rootedTr.source,protobuff_tx.rootedTr.destination,protobuff_tx.rootedTr.coins)
                                                                } else null,
        tx_type=protobuff_tx.txType)

    @JsonIgnore
    public fun getProtoBufferTx(): cs236351.txmanager.Tx {
        return tx {
            this.txId = this@Tx.tx_id
            this.inputs += this@Tx.inputs.map { uTxO {
                this.txId = it.tx_id
                this.address = it.address
                this.coins = it.coins
            } }
            this.outputs += this@Tx.outputs.map { tr {
                this.address = it.address
                this.coins = it.coins
            } }
            this.txType = this@Tx.tx_type
            if(this.txType == TxType.TransferBased) {
                this.rootedTr = rootedTr {
                    this.source = this@Tx.rooted_tr!!.source
                    this.destination = this@Tx.rooted_tr.destination
                    this.coins = this@Tx.rooted_tr.coins
                }
            }
        }
    }

    @JsonIgnore
    public fun isLegal(): Boolean {
        if(tx_type == TxType.TransferBased) {
            if(rooted_tr == null) return false
            return true
        } else {
            val expected_sender_addr: Address = inputs[0].address
            return (!inputs.any { it.address != expected_sender_addr }) &&
                    (inputs.sumOf { it.coins } == outputs.sumOf { it.coins }) &&
                    (!inputs.any { it.tx_id == tx_id })
        }
    }

    @JsonIgnore
    public fun getSenderAddress(): Address {
        assert(this.isLegal())
        return if(tx_type == TxType.TransferBased) {
            rooted_tr!!.source
        } else {
            inputs[0].address
        }
    }

    @JsonIgnore
    public fun getReceiverAddressList(): List<Address> {
        assert(this.isLegal())
        return if(tx_type == TxType.TransferBased) {
            listOf(rooted_tr!!.destination)
        } else {
            outputs.map { it.address }
        }
    }

    @JsonIgnore
    public fun populateID() {
        val id_generation_timestamp: String = java.time.ZonedDateTime.now().toString()
        val hash_function: HashFunction = Hashing.murmur3_128()
        val hc: HashCode = hash_function
            .newHasher()
            .putString(id_generation_timestamp + this.toString(), Charsets.UTF_8)
            .hash()
        this.tx_id = "0x${"$hc".uppercase(Locale.getDefault())}"
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

    public fun toList(): List<Tx> = tx_list
}