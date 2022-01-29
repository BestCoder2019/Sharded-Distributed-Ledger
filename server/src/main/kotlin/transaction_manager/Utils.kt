package transaction_manager

import io.grpc.ManagedChannel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.newSingleThreadContext
import zk_service.Ephemeral
import zk_service.ZooKeeperKt

internal val txManagerThread = newSingleThreadContext(
    name = "TxManagerThread")
    .asExecutor()
    .asCoroutineDispatcher()

fun getChannelEndPointID(channel: ManagedChannel): ID {
    return channel.authority().split(':')[1].toInt()
}

class FailedTransactionException: Exception() {}

class Membership private constructor(private val zk: ZooKeeperKt, val groupName: String) {
    var _id: String? = null
    val id: String get() = _id!!

    var onChange: (suspend () -> Unit)? = null

    suspend fun join(id: String) {
        val (_, stat) = zk.create {
            path = "/$id"
            flags = Ephemeral
        }
        _id = id
    }

    suspend fun queryMembers(): List<String> = zk.getChildren("/") {
        watchers += this@Membership.onChange?.let { { _, _, _ -> it() } }
    }.first

    suspend fun leave() {
        zk.delete("/$id")
    }

    companion object {
        suspend fun make(zk: ZooKeeperKt, groupName: String): Membership {
            val zk = zk.usingNamespace("/membership")
                .usingNamespace("/$groupName")
            return Membership(zk, groupName)
        }
    }
}
