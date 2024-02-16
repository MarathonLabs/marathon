package com.malinskiy.marathon.apple.cmd.remote.ssh.sshj

import com.malinskiy.marathon.apple.cmd.BaseCommandExecutorTest
import com.malinskiy.marathon.apple.cmd.remote.ssh.sshj.auth.SshAuthentication
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

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
