package multipaxos

import com.google.protobuf.ByteString
import com.google.protobuf.kotlin.toByteStringUtf8
import cs236351.multipaxos.*
import io.grpc.ManagedChannelBuilder
import io.grpc.ServerBuilder
import io.grpc.StatusException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import zk_service.*
import java.util.*
import java.util.concurrent.TimeUnit

val biSerializer = object : ByteStringBiSerializer<String> {
    override fun serialize(obj: String) = obj.toByteStringUtf8()
    override fun deserialize(serialization: ByteString) = serialization
        .toStringUtf8()!!
}

suspend fun main(args: Array<String>) = coroutineScope {

    // Displays all debug messages from gRPC
    // org.apache.log4j.BasicConfigurator.configure()

    // Take the ID as the port number
    val id = args[0].toInt()

    // Init services
    val learnerService = LearnerService(this)
    val acceptorService = AcceptorService(id)

    // Build gRPC server
    val server = ServerBuilder.forPort(id)
        .apply {
            if (id > 0) // Apply your own logic: who should be an acceptor
                addService(acceptorService)
        }
        .apply {
            if (id > 0) // Apply your own logic: who should be a learner
                addService(learnerService)
        }
        .build()

    val zk: ZooKeeperKt = ZookeeperKtClient()
    val omega = ZooKeeperOmegaFailureDetector.make(id,zk)

    // Create channels with clients
    val chans = listOf(8980, 8981, 8982).associateWith {
        ManagedChannelBuilder.forAddress("localhost", it).usePlaintext().build()!!
    }

    /*
    * Don't forget to add the list of learners to the learner service.
    * The learner service is a reliable broadcast service and needs to
    * have a connection with all processes that participate as learners
    */
    learnerService.learnerChannels = chans.filterKeys { it != id }

    // Use the atomic broadcast adapter to use the learner service as an atomic broadcast service
    val atomicBroadcast = AtomicBroadcastImpl(
        this,
        id,
        chans,
        biSerializer,
        omega,
    )

/*
    withContext(Dispatchers.IO) { // Operations that block the current thread should be in a IO context
        server.start()
    }
*/
    /*
     * You Should implement an omega failure detector.
     */

    // Create a proposer, not that the proposers id's id and
    // the acceptors id's must be all unique (they break symmetry)
    val proposer = Proposer(
        id = id, omegaFD = omega, scope = this, acceptors = chans.filterKeys { it != id },
        thisLearner = learnerService,
    )

    // Starts The proposer
    proposer.start()

    startRecievingMessages(atomicBroadcast)

    // "Key press" barrier so only one propser sends messages
    withContext(Dispatchers.IO) { // Operations that block the current thread should be in a IO context
        System.`in`.read()
    }
    startGeneratingMessages(id, proposer, true, atomicBroadcast)
    withContext(Dispatchers.IO) { // Operations that block the current thread should be in a IO context
        atomicBroadcast.close()
        server.awaitTermination()
    }
}

private fun CoroutineScope.startGeneratingMessages(
    id: Int,
    proposer: Proposer,
    useAtomicBroadcast: Boolean = false,
    atomicBroadcast: AtomicBroadcast<String>,
) {
    launch {
        println("Started Generating Messages")
        (1..100).forEach {
            delay(1000)
            val prop = "[Value no $it from $id]".toByteStringUtf8()
                .also { println("Adding Proposal ${it.toStringUtf8()!!}") }
            if(useAtomicBroadcast){
                atomicBroadcast._send(prop)
            } else {
                proposer.addProposal(prop)
            }
        }
    }
}

private fun CoroutineScope.startRecievingMessages(atomicBroadcast: AtomicBroadcast<String>) {
    launch {
        for ((`seq#`, msg) in atomicBroadcast.stream) {
            println("Message #$`seq#`: $msg  received!")
        }
    }
}