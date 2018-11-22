package com.malinskiy.marathon.ios.cmd.remote

import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream

interface CommandSession: Closeable {
    val inputStream: InputStream
    val errorStream: InputStream
    val outputStream: OutputStream

    fun connect(timeoutMillis: Long = CommandExecutor.DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS)
    fun join(timeoutMillis: Long = CommandExecutor.DEFAULT_SSH_CONNECTION_TIMEOUT_MILLIS)

    val exitStatus: Int?
}
