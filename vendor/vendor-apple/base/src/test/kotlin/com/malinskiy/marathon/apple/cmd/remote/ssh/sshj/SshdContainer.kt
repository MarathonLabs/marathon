package com.malinskiy.marathon.apple.cmd.remote.ssh.sshj

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName


class SshdContainer(
    imageName: DockerImageName
) : GenericContainer<SshdContainer?>(imageName) {
    private var username = "test-user"
    private var password = "test-password"

    constructor(imageName: String = "lscr.io/linuxserver/openssh-server:9.0_p1-r2-ls99") : this(DockerImageName.parse(imageName))

    init {
        imageName.assertCompatibleWith(DEFAULT_IMAGE_NAME)
        waitStrategy = Wait.forListeningPort()
        addExposedPort(SSH_PORT)
    }

    override fun configure() {
        addEnv("PASSWORD_ACCESS", "true")
        addEnv("USER_NAME", username)
        addEnv("USER_PASSWORD", password)
    }

    fun withUsername(username: String): SshdContainer {
        this.username = username
        return this
    }

    fun withPassword(password: String): SshdContainer {
        this.password = password
        return this
    }

    val url by lazy { "$username@$host:${getMappedPort(SSH_PORT)}"}

    companion object {
        private const val SSH_PORT = 2222
        private val DEFAULT_IMAGE_NAME = DockerImageName.parse("lscr.io/linuxserver/openssh-server:9.0_p1-r2-ls99")
    }
}
