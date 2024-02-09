package com.malinskiy.marathon.apple.ios.cmd

import java.io.File

interface FileBridge {
    suspend fun send(src: File, dst: String): Boolean
    suspend fun receive(src: String, dst: File): Boolean
}
