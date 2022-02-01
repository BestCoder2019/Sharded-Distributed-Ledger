
package com.example.api.controller

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.web.bind.annotation.*
import transaction_manager.*
import zk_service.ZooKeeperKt
import zk_service.ZookeeperKtClient


@RestController
class TxController() {

    //var ledgerService: LedgerService? = null

    @Autowired
    private lateinit var ledgerService: LedgerService

    @GetMapping("/clients/{address}/transactions")
    fun getClientTransactions(
        @PathVariable("address") address: Address,
        @RequestParam("num", defaultValue = "-1") num: Int,
    ): List<Tx> = ledgerService!!.getClientTxHistory(address,num).toList()

    @GetMapping("/clients/{address}/utxos")
    fun getClientUTxOs(
        @PathVariable("address") address: Address,
    ): List<UTxO> = ledgerService!!.getClientUTXO(address)

    @PostMapping("/transactions")
    fun createTransaction(@RequestBody payload: Tx): Tx = runBlocking {
        ledgerService!!.process(payload)
    }

    @PostMapping("/transactions/rootedTransfers")
    fun createRootedTrBasedTransaction(@RequestBody rooted_tr: RootedTr): Tx = runBlocking {
        ledgerService!!.process(Tx(rooted_tr))
    }
}

