package transaction_manager

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class TxRepo {
    private val timestamp_ordered_tx_queue: PriorityQueue<Tx> = PriorityQueue<Tx>
    // Comparator
    { o1, o2 ->
        java.time.ZonedDateTime.parse(o1!!.timestamp).compareTo(java.time.ZonedDateTime.parse(o2!!.timestamp))
    }
    private val tx_cache: ConcurrentMap<TxID, Tx> = ConcurrentHashMap()
    private val client_tx_cache: ConcurrentMap<Address, PriorityQueue<Tx>> = ConcurrentHashMap()
    private val client_utxo_cache: ConcurrentMap<Address, MutableSet<UTxO>> = ConcurrentHashMap()
    private val client_spent_txo_cache: ConcurrentMap<Address, MutableSet<UTxO>> = ConcurrentHashMap()

    private val mutex: Mutex = Mutex()

    /***
     * throws: FailedTransactionException on failed transactions. Or failed asserts exception.
     *         JVM -ea flag should be enabled
     *
     * returns: true if the tx was added by the invocation, false if the tx was already in the repo
     */
    public suspend fun commitTransaction(tx: Tx): Boolean = mutex.withLock {
        if(!tx.isLegal()) throw FailedTransactionException()
        if(tx_cache.contains(tx.tx_id)) return false

        val spender_address: Address = tx.getSenderAddress()
        val spender_spend_set = client_spent_txo_cache.computeIfAbsent(spender_address) {
            ConcurrentHashMap.newKeySet()
        }
        if((tx.inputs.toSet() intersect spender_spend_set).isNotEmpty()) throw FailedTransactionException()

        tx.timestamp = java.time.ZonedDateTime.now().toString()
        tx.inputs.forEach {
            tx_cache[it.tx_id]?.run {
                //println("txid=${it.tx_id} timestamp=${java.time.ZonedDateTime.parse(this.timestamp)}, this timestamp=${java.time.ZonedDateTime.parse(tx.timestamp)}")
                assert(java.time.ZonedDateTime.parse(this.timestamp) <= java.time.ZonedDateTime.parse(tx.timestamp))
            }
        }

        client_spent_txo_cache[spender_address]!! += (tx.inputs.toSet())
        client_utxo_cache.computeIfAbsent(spender_address) { ConcurrentHashMap.newKeySet() } -= tx.inputs.toSet()

        val result_utxo_list = tx.outputs.map {
            UTxO(tx.tx_id,it.address,it.coins)
        }
        result_utxo_list.forEach {
            if(!client_spent_txo_cache.computeIfAbsent(it.address) { ConcurrentHashMap.newKeySet() }.contains(it)) {
                val utxo_is_new: Boolean = client_utxo_cache.computeIfAbsent(it.address) { ConcurrentHashMap.newKeySet() }.add(it)
                assert(utxo_is_new)
            }
        }

        client_tx_cache.computeIfAbsent(spender_address) { PriorityQueue<Tx>
        // Comparator
            { o1, o2 ->
                java.time.ZonedDateTime.parse(o1!!.timestamp).compareTo(java.time.ZonedDateTime.parse(o2!!.timestamp))
            }
        }.add(tx)
        tx_cache[tx.tx_id] = tx
        timestamp_ordered_tx_queue.add(tx)
        return true
    }

    /***
     * un-does a commit. Should be called right after the unwanted commit without any commits that spend its transaction
     * output in between. This condition is asserted. Otherwise, we would have double spending.
     *
     * throws: failed asserts exception. JVM -ea flag should be enabled
     * returns: true if the tx was removed by the invocation, false if the tx wasn't in the repo
     */
    public suspend fun unCommitTransaction(tx: Tx): Boolean = mutex.withLock {
        if(!tx_cache.contains(tx.tx_id)) return false

        val spender_address: Address = tx.getSenderAddress()

        client_spent_txo_cache[spender_address]!! -= tx.inputs.toSet()
        client_utxo_cache[spender_address]!! += tx.inputs.toSet()

        val result_utxo_list = tx.outputs.map {
            UTxO(tx.tx_id,it.address,it.coins)
        }
        result_utxo_list.forEach {
            val txo_is_unspent: Boolean = client_utxo_cache[it.address]!!.remove(it)
            assert(txo_is_unspent)
        }

        client_tx_cache[spender_address]!!.remove(tx)
        tx_cache.remove(tx.tx_id)
        timestamp_ordered_tx_queue.remove(tx)

        return true
    }

    public fun contains(txid: TxID): Boolean {
        return tx_cache.contains(txid)
    }

    public fun getClientUTXO(address: Address): List<UTxO> {
        return client_utxo_cache.computeIfAbsent(address) { ConcurrentHashMap.newKeySet() }.toList()
    }

    public fun getClientTxHistory(address: Address): TxList {
        return TxList(client_tx_cache.computeIfAbsent(address) { PriorityQueue<Tx>
        // Comparator
        { o1, o2 ->
            java.time.ZonedDateTime.parse(o1!!.timestamp).compareTo(java.time.ZonedDateTime.parse(o2!!.timestamp))
        }
        }.toList())
    }

}