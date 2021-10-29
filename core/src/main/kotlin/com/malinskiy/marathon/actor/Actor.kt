package com.malinskiy.marathon.actor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.selects.SelectClause2
import kotlin.coroutines.CoroutineContext

abstract class Actor<in T>(
    parent: Job? = null,
    val context: CoroutineContext
) : SendChannel<T>, CoroutineScope {

    protected abstract suspend fun receive(msg: T)
    final override val coroutineContext: CoroutineContext
        get() = context + actorJob

    private val actorJob = Job(parent)

    @ObsoleteCoroutinesApi
    private val delegate = actor<T>(
        capacity = Channel.UNLIMITED,
        context = coroutineContext
    ) {
        for (msg in channel) {
            receive(msg)
        }
    }

    @ExperimentalCoroutinesApi
    override val isClosedForSend: Boolean
        get() = delegate.isClosedForSend

    override val onSend: SelectClause2<T, SendChannel<T>>
        get() = delegate.onSend

    @ExperimentalCoroutinesApi
    override fun invokeOnClose(handler: (cause: Throwable?) -> Unit) {
        delegate.invokeOnClose(handler)
    }

    override fun close(cause: Throwable?): Boolean {
        actorJob.cancel()
        return true
    }

    override fun offer(element: T): Boolean = delegate.offer(element)

    override fun trySend(element: T): ChannelResult<Unit> = delegate.trySend(element)

    override suspend fun send(element: T) = delegate.send(element)
}
