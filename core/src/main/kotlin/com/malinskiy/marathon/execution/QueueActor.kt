package com.malinskiy.marathon.execution

import com.malinskiy.marathon.aktor.Aktor
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.channels.Channel
import java.util.*

class QueueActor(configuration: Configuration,
                 testShard: TestShard) : Aktor<QueueMessage>() {

    //TODO: Use PriorityQueue instead of LinkedList
    private val queue: Queue<Test> = LinkedList<Test>(testShard.tests)
    private val batching = configuration.batchingStrategy

    override suspend fun receive(msg: QueueMessage) {
        when (msg) {
            is QueueMessage.RequestNext -> {
                requestNextBatch(msg.deferred)
            }
            is QueueMessage.Terminate -> {

            }
        }
    }

    private suspend fun requestNextBatch(deferred: Channel<QueueResponseMessage>) {
        if (queue.isNotEmpty()) {
            val batch = batching.process(queue)
            deferred.send(QueueResponseMessage.NextBatch(batch))
        } else {
            deferred.send(QueueResponseMessage.Empty)
        }
    }
}

sealed class QueueResponseMessage {
    data class NextBatch(val batch: TestBatch) : QueueResponseMessage()
    object Empty : QueueResponseMessage()
}

sealed class QueueMessage {
    data class RequestNext(val deferred: Channel<QueueResponseMessage>) : QueueMessage()
    object Terminate : QueueMessage()
}