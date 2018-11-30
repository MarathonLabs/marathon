package com.malinskiy.marathon.actor

import kotlinx.coroutines.channels.Channel

fun <T> unboundedChannel() = Channel<T>(Channel.UNLIMITED)
