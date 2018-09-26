package com.malinskiy.marathon.ios

import com.github.fracpete.processoutput4j.output.CollectingProcessOutput
import com.github.fracpete.rsync4j.RSync
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import java.io.File
import java.net.InetAddress

private const val PRODUCTS_PATH = "Build/Products"

class DerivedDataManager(val configuration: Configuration) {

    private val iosConfiguration: IOSConfiguration = configuration.vendorConfiguration as? IOSConfiguration
            ?: throw IllegalStateException("Expected an iOS configuration")

    private val logger = MarathonLogging.logger(javaClass.simpleName)

    val xctestrunPath: File
        get() {
            return productsDir
                .toPath()
                .relativize(
                    iosConfiguration
                        .xctestrunPath
                        .toPath()
                )
                .toFile()
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
    }

    private fun getSshString(port: Int): String {
        return "ssh -o 'StrictHostKeyChecking no' -F /dev/null " +
                "-vvv " +
                "-i ${iosConfiguration.remotePrivateKey} " +
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

private fun File.isDescendantOf(dir: File): Boolean {
    if (!dir.exists() || !dir.isDirectory) return false

    return canonicalFile.toPath().toAbsolutePath().startsWith(dir.canonicalFile.toPath().toAbsolutePath())
}