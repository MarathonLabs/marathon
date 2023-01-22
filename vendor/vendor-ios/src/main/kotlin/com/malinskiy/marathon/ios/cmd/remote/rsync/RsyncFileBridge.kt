package com.malinskiy.marathon.ios.cmd.remote.rsync

import com.github.fracpete.processoutput4j.output.CollectingProcessOutput
import com.github.fracpete.rsync4j.RSync
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.ios.SshAuthentication
import com.malinskiy.marathon.ios.cmd.FileBridge
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.sync.Mutex
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
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
) : FileBridge {
    private val logger = MarathonLogging.logger {}

    init {
        if (configuration.debug) {
            logger.debug(rsyncVersion)
        }
    }

    private val mutex = Mutex()

    override suspend fun send(src: File, dst: String): Boolean {
        mutex.withLock {
            val source = if (src.isDirectory) {
                src.absolutePathWithTrailingSeparator
            } else {
                src.absolutePath
            }
            val destination = "${target.addr}:$dst"

            val sshString = getSshString(target.port)
            val rsync = getRsyncBase()
                .rsh(sshString)
                .source(source)
                .destination(destination)

            return with(CollectingProcessOutput()) {
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

        val sshString = getSshString(target.port)
        val rsync = getRsyncBase()
            .links(true)
            .rsh(sshString)
            .source(source)
            .destination(destination)

        val output = CollectingProcessOutput()
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
            val output = CollectingProcessOutput()
            output.monitor(RSync().source("/tmp").destination("/tmp").version(true).builder())
            return output.stdOut.replace("""(?s)\n.*\z""".toRegex(), "")
        }

    private fun getRsyncBase(): RSync {
        return RSync()
            .a()
            .partial(true)
            .partialDir(".rsync-partial")
            .delayUpdates(true)
            .rsyncPath(vendorConfiguration.rsync.remotePath)
            .verbose(configuration.debug)
    }

    private fun getSshString(port: Int): String {
        val authentication = vendorConfiguration.ssh.authentication as? SshAuthentication.PublicKeyAuthentication
            ?: throw ConfigurationException("rsync bridge supports only public-key ssh auth")

        return "ssh -o 'StrictHostKeyChecking no' -F /dev/null " +
            "-i ${authentication.key} " +
            "-l ${authentication.username} " +
            "-p $port " +
            when (configuration.debug && vendorConfiguration.ssh.debug) {
                true -> "-vvv"
                else -> ""
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
