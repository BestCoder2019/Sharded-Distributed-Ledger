package multipaxos

import com.google.protobuf.ByteString
import com.google.protobuf.empty
import cs236351.multipaxos.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import transaction_manager.Tx
import java.util.Scanner
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.coroutines.CoroutineContext
import cs236351.multipaxos.AtomicBroadcastSequencerServiceGrpcKt.AtomicBroadcastSequencerServiceCoroutineImplBase as SequencerGrpcImplBase

class SequencerService(
    val proposer: Proposer,
    private val scope: CoroutineScope,
    context: CoroutineContext = paxosThread,
) : SequencerGrpcImplBase(context) {
    private val last_message_cache: ConcurrentMap<ID, String> = ConcurrentHashMap()
    private val mutex: Mutex = Mutex()

    override suspend fun broadcastMessage(request: Message): AckMessage {
        val message: ByteString = request.value
        var request_string = biSerializer(message)

        val scanner = Scanner(request_string).useDelimiter(":")
        val sender_id = try {
            scanner.next().removePrefix("@").toInt()
        } catch (e: Exception) {
            println("Sequencer: Message format is wrong")
            println(e)
            return ackMessage {
                this.ack = Ack.NO
            }
        }
        request_string = request_string.removePrefix("@$sender_id:")
        mutex.lock()
        val last_message: String? = last_message_cache[sender_id]
        if((last_message != null) && (last_message == request_string)) {
            mutex.unlock()
            return ackMessage {
                this.ack = Ack.YES
            }
        }
        last_message_cache[sender_id] = request_string
        mutex.unlock()

        proposer.addProposal(message)
        return ackMessage {
            this.ack = Ack.YES
        }
    }
}
