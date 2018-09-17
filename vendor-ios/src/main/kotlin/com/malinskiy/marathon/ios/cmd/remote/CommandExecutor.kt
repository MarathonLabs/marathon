package com.malinskiy.marathon.ios.cmd.remote

import net.schmizz.sshj.connection.channel.direct.Session

interface CommandExecutor {
    fun startSession(): Session
}
