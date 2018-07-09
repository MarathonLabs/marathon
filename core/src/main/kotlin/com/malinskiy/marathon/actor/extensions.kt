package com.malinskiy.marathon.actor

import kotlinx.coroutines.experimental.channels.Channel

fun <T> unboundedChannel() = Channel<T>(Channel.UNLIMITED)