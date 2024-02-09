package com.malinskiy.marathon.ios.cmd.remote.ssh.sshj

import com.malinskiy.marathon.apple.ios.cmd.remote.ssh.sshj.auth.SshAuthentication
import com.malinskiy.marathon.ios.cmd.BaseCommandExecutorTest
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class SshjCommandExecutorTest : BaseCommandExecutorTest() {
    @Container
    private val sshdContainer: SshdContainer = SshdContainer()
        .withUsername("foo")
        .withPassword("secret")

    override fun createExecutor() = com.malinskiy.marathon.apple.ios.cmd.remote.ssh.sshj.SshjCommandExecutorFactory()
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
