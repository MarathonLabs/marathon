package com.malinskiy.marathon.ios.cmd.remote.ssh.sshj

import com.malinskiy.marathon.ios.cmd.remote.ssh.sshj.auth.SshAuthentication
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Duration

class SshTest {
    @Test
    fun testSigint() = runBlocking {
//        SshjCommandExecutorFactory()
//            .connect(
//                "192.168.2.22",
//                22,
//                authentication = SshAuthentication.PublicKeyAuthentication("malinskiy", File("/home/pkunzip/.ssh/id_ed25519")),
//                hostKeyVerifier = OpenSSHKnownHosts(File("/home/pkunzip/.ssh/known_hosts")),
//                true
//            ).use { executor ->
//                val session = executor.execute(
//                    listOf(
//                        "tail", "-f", "/var/log/system.log", "&"
//                    ), Duration.ofMinutes(10), Duration.ofMinutes(10), emptyMap(), null
//                )
//                val result = async {
//                    session.await()
//                }
//                delay(1000)
//                executor.execute(
//                    listOf(
//                        ""
//                    )
//                )
//                session.interrupt()
//
//                println(result)
//            }
    }
}
