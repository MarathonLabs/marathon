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

@Testcontainers
class SshjCommandExecutorTest : BaseCommandExecutorTest() {
    @Container
    private val sshdContainer: SshdContainer = SshdContainer()
        .withUsername("foo")
        .withPassword("secret")

    override fun createExecutor() = SshjCommandExecutorFactory()
        .connect(
            sshdContainer.host,
            sshdContainer.firstMappedPort,
            SshAuthentication.PasswordAuthentication(
                "foo",
                "secret"
            ),
            debug = true,
        )
}
