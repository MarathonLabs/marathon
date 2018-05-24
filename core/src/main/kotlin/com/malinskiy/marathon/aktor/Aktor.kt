package com.malinskiy.marathon.aktor

import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.selects.SelectClause2

abstract class Aktor<T> : SendChannel<T> {

    protected abstract suspend fun receive(msg: T)

    private val delegate = actor<T> {
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

    override fun close(cause: Throwable?): Boolean = delegate.close(cause)

    override fun offer(element: T): Boolean = delegate.offer(element)

    override suspend fun send(element: T) = delegate.send(element)
}
