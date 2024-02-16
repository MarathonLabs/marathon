package com.malinskiy.marathon.apple.cmd.remote.ssh.sshj

import com.malinskiy.marathon.apple.cmd.CommandHost
import com.malinskiy.marathon.apple.cmd.remote.ssh.sshj.auth.SshAuthentication
import java.nio.charset.Charset

/**
 * Holds SSH coordinates.
 *
 * @param addr IP of the remote system
 * @param port Port of the remote system
 * @param username User allowed to connect to the remote server
 * @param authentication Private SSH authentication method for the user
 * @param charset of the remote system
 */
data class SshjCommandHost(
    val addr: String,
    val port: Int,
    val authentication: SshAuthentication,
    override val charset: Charset,
    override val id: String = "${authentication.username}@$addr:$port",
) : CommandHost
