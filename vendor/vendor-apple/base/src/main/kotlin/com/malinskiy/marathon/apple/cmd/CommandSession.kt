package com.malinskiy.marathon.apple.cmd

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.ReceiveChannel

interface CommandSession : AutoCloseable {
    val stdout: ReceiveChannel<String>
    val stderr: ReceiveChannel<String>
    val exitCode: Deferred<Int?>
    val isAlive: Boolean

    suspend fun await(): CommandResult
    suspend fun drain()
    fun terminate()
    fun kill()
}
