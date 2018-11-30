package com.malinskiy.marathon.actor

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel

fun <T> unboundedChannel() = Channel<T>(Channel.UNLIMITED)

suspend fun <T> SendChannel<T>.safeSend(element: T) {
    if(isClosedForSend) return
    send(element)
}