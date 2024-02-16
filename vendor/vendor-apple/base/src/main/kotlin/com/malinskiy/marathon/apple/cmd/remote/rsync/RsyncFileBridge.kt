package com.malinskiy.marathon.apple.cmd.remote.rsync

import com.github.fracpete.rsync4j.RSync
import com.malinskiy.marathon.apple.cmd.FileBridge
import com.malinskiy.marathon.apple.cmd.remote.rsync.a
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Lock

/**
 * Each device will share the same instance of file bridge, but the concurrent requests should still be handled
 * The easiest solution for now is to allow 1 send and 1 receive at a time. The callers will suspend properly though.
 *
 * A better solution would be to allow non-overlapping paths e.g. /tmp/marathon/x and /tmp/marathon/y should be allowed, but /tmp/marathon/x
 * and /tmp/marathon should lock and wait for mutex.
 */
class RsyncFileBridge(
    private val target: RsyncTarget,
    private val configuration: com.malinskiy.marathon.config.Configuration,
    private val vendorConfiguration: com.malinskiy.marathon.config.vendor.VendorConfiguration.IOSConfiguration,
    private val authentication: com.malinskiy.marathon.config.vendor.apple.SshAuthentication?,
) : FileBridge {
    private val logger = com.malinskiy.marathon.log.MarathonLogging.logger {}

    init {
        if (configuration.debug) {
            logger.debug(rsyncVersion)
        }
    }

    private val mutex = kotlinx.coroutines.sync.Mutex()

    override suspend fun send(src: File, dst: String): Boolean {
        mutex.withLock {
            val source = if (src.isDirectory) {
                src.absolutePathWithTrailingSeparator
            } else {
                src.absolutePath
            }
            val destination = "${target.addr}:$dst"

            val rsync = getRsyncBase()
                .authenticate()
                .source(source)
                .destination(destination)

            return with(com.github.fracpete.processoutput4j.output.CollectingProcessOutput()) {
                monitor(rsync.builder())
                if (exitCode != 0) {
                    if (stdErr.isNotEmpty()) {
                        logger.error { "send error: $stdErr" }
                    }
                    false
                } else {
                    true
                }
            }
        }
    }

    override suspend fun receive(src: String, dst: File): Boolean {
        val source = "${target.addr}:$src"
        val destination = if (dst.isDirectory) {
            dst.absolutePathWithTrailingSeparator
        } else {
            dst.absolutePath
        }

        val rsync = getRsyncBase()
            .links(true)
            .authenticate()
            .source(source)
            .destination(destination)

        val output = com.github.fracpete.processoutput4j.output.CollectingProcessOutput()
        output.timeOut = 30
        output.monitor(rsync.builder())
        if (output.exitCode != 0) {
            if (output.stdErr.isNotEmpty()) {
                logger.error(output.stdErr)
            }
        }
        return output.exitCode == 0
    }

    private val rsyncVersion: String
        get() {
            val output = com.github.fracpete.processoutput4j.output.CollectingProcessOutput()
            output.monitor(com.github.fracpete.rsync4j.RSync().source("/tmp").destination("/tmp").version(true).builder())
            return output.stdOut.replace("""(?s)\n.*\z""".toRegex(), "")
        }

    private fun getRsyncBase(): com.github.fracpete.rsync4j.RSync {
        return com.github.fracpete.rsync4j.RSync()
            .a()
            .partial(true)
            .partialDir(".rsync-partial")
            .delayUpdates(true)
            .rsyncPath(vendorConfiguration.rsync.remotePath)
            .verbose(configuration.debug)
    }

    private fun com.github.fracpete.rsync4j.RSync.authenticate(): com.github.fracpete.rsync4j.RSync {
        return when (authentication) {
            is com.malinskiy.marathon.config.vendor.apple.SshAuthentication.PasswordAuthentication -> {
                sshPass(
                    com.github.fracpete.rsync4j.SshPass().password(authentication.password)
                ).rsh(
                    "ssh -o 'StrictHostKeyChecking no' -F /dev/null " +
                        "-l ${authentication.username} " +
                        "-p ${target.port} " +
                        when (configuration.debug && vendorConfiguration.ssh.debug) {
                            true -> "-vvv"
                            else -> ""
                        }
                )
            }
            is com.malinskiy.marathon.config.vendor.apple.SshAuthentication.PublicKeyAuthentication -> {
                rsh(
                    "ssh -o 'StrictHostKeyChecking no' -F /dev/null " +
                        "-i ${authentication.key} " +
                        "-l ${authentication.username} " +
                        "-p ${target.port} " +
                        when (configuration.debug && vendorConfiguration.ssh.debug) {
                            true -> "-vvv"
                            else -> ""
                        }
                )
            }
            null -> throw com.malinskiy.marathon.config.exceptions.ConfigurationException("rsync bridge needs ssh auth")
        }
    }

    private val File.absolutePathWithTrailingSeparator: String
        get() {
            return absolutePath.dropLastWhile { it == File.separatorChar } + File.separatorChar
        }

    companion object {
        private val hostnameLocksMap = ConcurrentHashMap<String, Lock>()
    }
}

private fun RSync.a(): RSync {
    return this
        .recursive(true)
        .links(true)
        .perms(true)
        .times(true)
        .group(true)
        .owner(true)
        .devices(true)
        .specials(true)
}

private fun File.isDescendantOf(dir: File): Boolean {
    if (!dir.exists() || !dir.isDirectory) return false

    return canonicalFile.toPath().toAbsolutePath().startsWith(dir.canonicalFile.toPath().toAbsolutePath())
}
