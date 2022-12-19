package com.malinskiy.marathon.ios.cmd.local

import com.malinskiy.marathon.ios.cmd.FileBridge
import java.io.File

class JvmFileBridge(private val overwrite: Boolean = true) : FileBridge {
    override suspend fun send(src: File, dst: String): Boolean {
        return src.copyRecursively(File(dst), overwrite = overwrite)
    }

    override suspend fun receive(src: String, dst: File): Boolean {
        return File(src).copyRecursively(dst, overwrite = overwrite)
    }
}
