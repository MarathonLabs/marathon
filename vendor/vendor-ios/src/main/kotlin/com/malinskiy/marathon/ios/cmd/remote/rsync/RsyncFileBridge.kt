package com.malinskiy.marathon.ios.cmd.remote.rsync

import com.github.fracpete.processoutput4j.output.CollectingProcessOutput
import com.github.fracpete.rsync4j.RSync
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.ios.cmd.FileBridge
import com.malinskiy.marathon.log.MarathonLogging
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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
            .rsyncPath(vendorConfiguration.remoteRsyncPath)
            .verbose(configuration.debug)
    }

    private fun getSshString(port: Int): String {
        return "ssh -o 'StrictHostKeyChecking no' -F /dev/null " +
            "-i ${vendorConfiguration.remotePrivateKey} " +
            "-l ${vendorConfiguration.remoteUsername} " +
            "-p $port " +
            when (configuration.debug && vendorConfiguration.debugSsh) {
                true -> "-vvv"
                else -> ""
            }
    }

    override suspend fun send(src: File, dst: String): Boolean {
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
