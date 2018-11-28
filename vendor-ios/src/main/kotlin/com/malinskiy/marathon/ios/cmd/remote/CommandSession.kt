package com.malinskiy.marathon.ios.cmd.remote

import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream

interface CommandSession: Closeable {
    val inputStream: InputStream
    val errorStream: InputStream
    val outputStream: OutputStream

    val isEOF: Boolean
    val isOpen: Boolean

    val exitStatus: Int?
}
