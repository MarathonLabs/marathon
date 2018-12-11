package com.malinskiy.marathon.actor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.selects.SelectClause2
import kotlin.coroutines.CoroutineContext

abstract class Actor<in T>(parent: Job? = null,
                           val context: CoroutineContext) : SendChannel<T>, CoroutineScope {

    protected abstract suspend fun receive(msg: T)
    override val coroutineContext: CoroutineContext
        get() = context + actorJob

    private val actorJob = Job(parent)

    private val delegate = actor<T>(
            capacity = Channel.UNLIMITED,
            context = coroutineContext
    ) {
        for (msg in channel) {
            receive(msg)
        }
    }

    override val isClosedForSend: Boolean
        get() = delegate.isClosedForSend
    override val isFull: Boolean
        get() = delegate.isFull
    override val onSend: SelectClause2<T, SendChannel<T>>
        get() = delegate.onSend

    override fun invokeOnClose(handler: (cause: Throwable?) -> Unit) {
        delegate.invokeOnClose(handler)
    }

    override fun close(cause: Throwable?): Boolean {
        actorJob.cancel()
        return true
    }

    override fun offer(element: T): Boolean = delegate.offer(element)

    override suspend fun send(element: T) = delegate.send(element)
}
