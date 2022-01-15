package multipaxos

import kotlinx.coroutines.*
import org.apache.zookeeper.KeeperException
import zk_service.*

class ZooKeeperOmegaFailureDetector(
    val id: ID,
    val zk: ZooKeeperKt
) : OmegaFailureDetector<ID> {

    private var _leader: ID = id
    override val leader: ID get() = _leader
    private var on_change_list: List<(suspend () -> Unit)> = mutableListOf()
    suspend fun updateLeader(): Unit {
        val on_change_update_leader: (suspend () -> Unit) = ::updateLeader
        val children_map = zk.getChildren("/"){
            this.watchers += on_change_update_leader.let{ { _,_,_ -> it() } }
        }.first
            .associate{
                Pair(it.removeSuffix("-${ZKPaths.extractSequentialSuffix(it)!!}").toInt(),
                    ZKPaths.extractSequentialSuffix(it)!!.toInt())
            }

        _leader = children_map.toList().sortedBy { (_, value) -> value }[0].first
        runBlocking {
            for (watcher in this@ZooKeeperOmegaFailureDetector.on_change_list.map { it!! }) {
                launch {
                    watcher()
                }
            }
        }
    }

    override fun addWatcher(observer: suspend () -> Unit) {
        on_change_list = on_change_list + observer
    }

    companion object {
        suspend fun make(id: ID, zk: ZooKeeperKt): ZooKeeperOmegaFailureDetector {
            val zk = zk.usingNamespace("/Election")
            zk.create {
                path = "/${id}-"
                flags = Ephemeral and Sequential
                ignore(KeeperException.Code.NODEEXISTS)
            }

            val omega = ZooKeeperOmegaFailureDetector(id, zk)
            omega.updateLeader()

            return omega
        }
    }
}