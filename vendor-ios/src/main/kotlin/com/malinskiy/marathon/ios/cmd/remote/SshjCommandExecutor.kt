package com.malinskiy.marathon.ios.cmd.remote

import com.malinskiy.marathon.log.MarathonLogging
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.LoggerFactory
import org.slf4j.Logger
import java.io.File
import java.net.InetAddress

class SshjCommandExecutor(val hostAddress: InetAddress,
                          val remoteUsername: String,
                          val remotePublicKey: File,
                          val sshPort: Int = 22) : CommandExecutor {

    val ssh: SSHClient

    init {
        val config = DefaultConfig()
        val loggerFactory = object : LoggerFactory {
            override fun getLogger(clazz: Class<*>?): Logger = MarathonLogging.logger(clazz?.simpleName ?: SshjCommandExecutor::class.java.simpleName)
            override fun getLogger(name: String?): Logger = MarathonLogging.logger(name ?: "")
        }
        config.loggerFactory = loggerFactory

        ssh = SSHClient(config)
        ssh.loadKnownHosts()
        val keys = ssh.loadKeys(remotePublicKey.absolutePath)
        ssh.connect("localhost")
        ssh.authPublickey(remoteUsername, keys)
    }

    override fun startSession() = ssh.startSession()
}
