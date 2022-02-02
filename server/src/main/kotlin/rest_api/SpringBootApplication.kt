package com.example.api

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import transaction_manager.ID
import transaction_manager.LedgerService
import zk_service.ZooKeeperKt
import zk_service.ZookeeperKtClient


@SpringBootApplication
class LedgerApplication(@Value("\${ledger.id}")  private val id: ID) {
	val mutex: Mutex = Mutex()

	@EventListener(ContextRefreshedEvent::class)
	@Bean
	fun ledgerService(): LedgerService = runBlocking {
		mutex.lock()

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

		val zk: ZooKeeperKt = ZookeeperKtClient()

		val ledgerService: LedgerService = LedgerService.make(
			GlobalScope,
			zk,
			id,
			shard_id_list,
			shard_broadcast_channels,
			global_broadcast_channels,
			shard_atomic_broadcast_id,
			global_atomic_broadcast_id,
			global_ledger_channels,
		)
		mutex.unlock()

		ledgerService
	}
}

fun main(args: Array<String>) {
	org.apache.log4j.BasicConfigurator.configure()
	runApplication<LedgerApplication>(*args)
}
