package com.malinskiy.marathon.apple.ios.cmd.remote.scp

import com.malinskiy.marathon.apple.ios.cmd.FileBridge
import com.malinskiy.marathon.log.MarathonLogging
import net.schmizz.sshj.SSHClient
import java.io.File
import java.io.IOException

class ScpFileBridge(private val ssh: SSHClient) : FileBridge {
    private val logger = MarathonLogging.logger {}
    override suspend fun send(src: File, dst: String): Boolean {
        try {
            val scpFileTransfer = ssh.newSCPFileTransfer()
            scpFileTransfer.upload(src.absolutePath, dst)
        } catch (e: IllegalStateException) {
            logger.error(e) { "unable to send $src" }
            return false
        } catch (e: IOException) {
            logger.error(e) { "unable to send $src" }
            return false
        }
        
        return true
    }

    override suspend fun receive(src: String, dst: File): Boolean {
        try {
            val scpFileTransfer = ssh.newSCPFileTransfer()
            scpFileTransfer.download(src, dst.toString())
        } catch (e: IllegalStateException) {
            logger.error(e) { "unable to send $src" }
            return false
        } catch (e: IOException) {
            logger.error(e) { "unable to send $src" }
            return false
        }
        return true
    }
}
