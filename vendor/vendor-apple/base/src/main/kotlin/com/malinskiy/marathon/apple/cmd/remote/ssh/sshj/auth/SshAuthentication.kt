package com.malinskiy.marathon.apple.cmd.remote.ssh.sshj.auth

import net.schmizz.sshj.SSHClient
import java.io.File

sealed class SshAuthentication {
    abstract val username: String
    abstract fun authenticate(sshClient: SSHClient)

    data class PasswordAuthentication(
        override val username: String,
        private val password: String
    ) : SshAuthentication() {
        override fun authenticate(sshClient: SSHClient) {
            sshClient.authPassword(username, password)
        }
    }

    data class PublicKeyAuthentication(
        override val username: String,
        private val key: File,
    ) : SshAuthentication() {
        override fun authenticate(sshClient: SSHClient) {
            val keys = sshClient.loadKeys(key.absolutePath)
            sshClient.authPublickey(username, keys)
        }
    }

}
