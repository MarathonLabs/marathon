package com.malinskiy.marathon.apple.cmd.local

import com.malinskiy.marathon.apple.RemoteFileManager
import com.malinskiy.marathon.apple.cmd.FileBridge
import java.io.File

class JvmFileBridge(private val overwrite: Boolean = true) : FileBridge {
    override suspend fun send(src: File, dst: String): Boolean {
        return src.copyRecursively(File(dst), overwrite = overwrite)
    }

    override suspend fun receive(src: String, dst: File): Boolean {
        val filename = src.split(RemoteFileManager.FILE_SEPARATOR).last()
        val destination = if (dst.isDirectory) {
            File(dst, filename)
        } else {
            dst
        }
        return File(src).copyRecursively(destination, overwrite = overwrite)
    }
}
