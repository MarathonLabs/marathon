package com.malinskiy.marathon.ios.cmd.remote.ssh.sshj

import com.malinskiy.marathon.config.vendor.ios.SshAuthentication.PublicKeyAuthentication
import com.malinskiy.marathon.ios.cmd.BaseCommandExecutorTest
import com.malinskiy.marathon.ios.cmd.remote.ssh.sshj.auth.SshAuthentication
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.time.Duration

class DarwinCommandExecutorTest : BaseCommandExecutorTest() {
    override fun createExecutor() = SshjCommandExecutorFactory()
        .connect(
            "192.168.2.22",
            22,
            SshAuthentication.PublicKeyAuthentication(
                "malinskiy",
                File("/home/pkunzip/.ssh/id_ed25519"),
            ),
            debug = true,
        )
}
