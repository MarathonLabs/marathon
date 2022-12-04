package com.malinskiy.marathon.ios

import com.github.fracpete.processoutput4j.output.CollectingProcessOutput
import com.github.fracpete.rsync4j.RSync
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.extension.relativePathTo
import com.malinskiy.marathon.log.MarathonLogging
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private const val PRODUCTS_PATH = "Build/Products"

class DerivedDataManager(val configuration: Configuration) {
    companion object {
        private val hostnameLocksMap = ConcurrentHashMap<String, Lock>()
    }

    private val logger = MarathonLogging.logger(javaClass.simpleName)

    private val iosConfiguration: VendorConfiguration.IOSConfiguration =
        configuration.vendorConfiguration as VendorConfiguration.IOSConfiguration

    val productsDir: File
        get() = iosConfiguration.derivedDataDir.resolve(PRODUCTS_PATH)

    val xctestrunFile: File
        get() = iosConfiguration.safecxtestrunPath()

    init {
        if (configuration.debug) {
            logger.debug(rsyncVersion)
        }
        if (!iosConfiguration.remotePrivateKey.exists()) {
            throw FileNotFoundException("Private key not found at ${iosConfiguration.remotePrivateKey}")
        }
        if (xctestrunFile.relativePathTo(productsDir) != xctestrunFile.name) {
            throw FileNotFoundException("xctestrun file must be located in build products directory.")
        }
    }

    private val rsyncVersion: String
        get() {
            val output = CollectingProcessOutput()
            output.monitor(RSync().source("/tmp").destination("/tmp").version(true).builder())
            return output.stdOut.replace("""(?s)\n.*\z""".toRegex(), "")
        }

    fun sendSynchronized(localPath: File, remotePath: String, hostName: String, port: Int) {
        hostnameLocksMap.getOrPut(hostName) { ReentrantLock() }.withLock {
            send(localPath, remotePath, hostName, port)
        }
    }

    fun send(localPath: File, remotePath: String, hostName: String, port: Int) {
        val source = if (localPath.isDirectory) {
            localPath.absolutePathWithTrailingSeparator
        } else {
            localPath.absolutePath
        }
        val destination = "$hostName:$remotePath"

        val sshString = getSshString(port)
        val rsync = getRsyncBase()
            .rsh(sshString)
            .source(source)
            .destination(destination)

        val output = CollectingProcessOutput()
        output.monitor(rsync.builder())
        if (output.exitCode != 0) {
            if (output.stdErr.isNotEmpty()) {
                logger.error(output.stdErr)
            }
        }
    }

    fun receive(remotePath: String, hostName: String, port: Int, localPath: File): Int {
        val source = "$hostName:$remotePath"
        val destination = if (localPath.isDirectory) {
            localPath.absolutePathWithTrailingSeparator
        } else {
            localPath.absolutePath
        }

        val sshString = getSshString(port)
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
        return output.exitCode
    }

    private fun getRsyncBase(): RSync {
        return RSync()
            .a()
            .partial(true)
            .partialDir(".rsync-partial")
            .delayUpdates(true)
            .rsyncPath(iosConfiguration.remoteRsyncPath)
            .verbose(configuration.debug)
    }

    private fun getSshString(port: Int): String {
        return "ssh -o 'StrictHostKeyChecking no' -F /dev/null " +
            "-i ${iosConfiguration.remotePrivateKey} " +
            "-l ${iosConfiguration.remoteUsername} " +
            "-p ${port.toString()} " +
            when (configuration.debug && iosConfiguration.debugSsh) {
                true -> "-vvv"
                else -> ""
            }
    }
}

private val File.absolutePathWithTrailingSeparator: String
    get() {
        return absolutePath.dropLastWhile { it == File.separatorChar } + File.separatorChar
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
