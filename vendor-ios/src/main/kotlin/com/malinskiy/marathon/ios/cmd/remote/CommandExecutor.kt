package com.malinskiy.marathon.ios.cmd.remote

import net.schmizz.sshj.connection.channel.direct.Session

const val DEFAULT_TIMEOUT = 5L

data class CommandResult(val stdout: String, val stderr: String, val exitStatus: Int)

interface CommandExecutor {
    fun startSession(): Session
    fun exec(command: String, timeout: Long = DEFAULT_TIMEOUT): CommandResult
}