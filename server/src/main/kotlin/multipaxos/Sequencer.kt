package multipaxos

import com.google.protobuf.ByteString
import com.google.protobuf.empty
import cs236351.multipaxos.*
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import cs236351.multipaxos.AtomicBroadcastSequencerServiceGrpcKt.AtomicBroadcastSequencerServiceCoroutineImplBase as SequencerGrpcImplBase

class SequencerService(
    val proposer: Proposer,
    private val scope: CoroutineScope,
    context: CoroutineContext = paxosThread,
):  SequencerGrpcImplBase(context){

    override suspend fun broadcastMessage(request: Message): AckMessage {
        val message: ByteString = request.value
        proposer.addProposal(message)
        return ackMessage {
            this.ack = Ack.YES
        }
    }
}
