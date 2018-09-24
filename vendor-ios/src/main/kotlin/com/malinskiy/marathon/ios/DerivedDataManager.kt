package com.malinskiy.marathon.ios

import com.github.fracpete.processoutput4j.output.CollectingProcessOutput
import com.github.fracpete.rsync4j.RSync
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import java.io.File

private const val STANDARD_SSH_PORT = 22
private const val PRODUCTS_PATH = "Build/Products"

class DerivedDataManager(val configuration: Configuration) {

    private val iosConfiguration: IOSConfiguration = configuration.vendorConfiguration as? IOSConfiguration
            ?: throw IllegalStateException("Expected an iOS configuration")

    private val logger = MarathonLogging.logger(javaClass.simpleName)

    private val xctestrun: File = iosConfiguration.xctestrunPath

    val productsDir: File
        get() {
            return iosConfiguration
                .derivedDataDir
                .toPath()
                .resolve(PRODUCTS_PATH)
                .toFile()
        }

    fun send(localPath: File, remotePath: String, hostname: String, port: Int = STANDARD_SSH_PORT) {

        val source= if (localPath.isDirectory) {
            localPath.absolutePathWithTrailingSeparator
        } else {
            localPath.absolutePath
        }
        val destination = "$hostname:$remotePath"

        val rsync = getRsyncBase()
                .rsh(getSshString(port))
                .source(source)
                .destination(destination)

        val output = CollectingProcessOutput()
        output.monitor(rsync.builder())
        if (output.stdErr.isNotEmpty()) {
            logger.error(output.stdErr)
        }
    }

    fun receive(remotePath: String, hostname: String, port: Int = STANDARD_SSH_PORT, localPath: File) {
        val source = "$hostname:$remotePath"
        val destination = localPath.absolutePath

        val rsync = getRsyncBase()
                .rsh(getSshString(port))
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
    }

    private fun getSshString(port: Int): String {
        return "ssh -o 'StrictHostKeyChecking no' -F /dev/null " +
                "${if (configuration.debug) "-vvv" else ""} " +
                "-i ${iosConfiguration.remotePublicKey} " +
                "-l ${iosConfiguration.remoteUsername} " +
                "-p ${port.toString()}"
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
