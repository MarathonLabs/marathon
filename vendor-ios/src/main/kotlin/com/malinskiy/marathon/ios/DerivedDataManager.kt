package com.malinskiy.marathon.ios

import com.github.fracpete.processoutput4j.output.CollectingProcessOutput
import com.github.fracpete.rsync4j.RSync
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import java.io.File
import java.io.FileNotFoundException

private const val PRODUCTS_PATH = "Build/Products"

class DerivedDataManager(val configuration: Configuration) {

    private val logger = MarathonLogging.logger(javaClass.simpleName)

    private val iosConfiguration: IOSConfiguration

    init {
        iosConfiguration = configuration.vendorConfiguration as? IOSConfiguration
                ?: throw IllegalStateException("Expected an iOS configuration")
        if (!iosConfiguration.remotePrivateKey.exists()) {
            throw FileNotFoundException("Private key not found at ${iosConfiguration.remotePrivateKey}")
        }
    }

    val productsDir: File
        get() {
            return iosConfiguration
                .derivedDataDir
                .toPath()
                .resolve(PRODUCTS_PATH)
                .toFile()
        }

    fun send(localPath: File, remotePath: String, hostName: String, port: Int) {

        val source= if (localPath.isDirectory) {
            localPath.absolutePathWithTrailingSeparator
        } else {
            localPath.absolutePath
        }
        val destination = "$hostName:$remotePath"

        val sshString = getSshString(port)
        logger.debug { "Using ssh string ${sshString}" }
        val rsync = getRsyncBase()
                .rsh(sshString)
                .source(source)
                .destination(destination)

        val output = CollectingProcessOutput()
        output.monitor(rsync.builder())
        if (output.stdErr.isNotEmpty()) {
            logger.error(output.stdErr)
        }
    }

    fun receive(remotePath: String, hostName: String, port: Int, localPath: File) {
        val source = "$hostName:$remotePath"
        val destination = localPath.absolutePath

        val sshString = getSshString(port)
        logger.debug { "Using ssh string ${sshString}" }
        val rsync = getRsyncBase()
                .rsh(sshString)
                .source(source)
                .destination(destination)

        val output = CollectingProcessOutput()
        output.monitor(rsync.builder())
        if (output.stdErr.isNotEmpty()) {
            logger.error(output.stdErr)
        }
    }

    private fun getRsyncBase(): RSync {
        return RSync()
                .a()
                .partial(true)
                .delete(true)
                .verbose(configuration.debug)
                .partialDir(".rsync-partial")
                .delayUpdates(true)
                .deleteDelay(true)
    }

    private fun getSshString(port: Int): String {
        return "ssh -o 'StrictHostKeyChecking no' -F /dev/null " +
                "-i ${iosConfiguration.remotePrivateKey} " +
                "-l ${iosConfiguration.remoteUsername} " +
                "-p ${port.toString()}" +
                when (configuration.debug && iosConfiguration.debugSsh) { true -> "-vvv" else -> ""}
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
