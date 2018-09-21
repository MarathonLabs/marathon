package com.malinskiy.marathon.ios

import com.github.fracpete.processoutput4j.output.CollectingProcessOutput
import com.github.fracpete.rsync4j.RSync
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import java.io.File

class DerivedDataManager(val configuration: Configuration,
                         val hostname: String,
                         val sshPort: Int) {

    private val iosConfiguration: IOSConfiguration = configuration.vendorConfiguration as? IOSConfiguration
            ?: throw IllegalStateException("Expected an iOS configuration")

    private val logger = MarathonLogging.logger(javaClass.simpleName)

    private val xctestrun: File
        get() {
            return iosConfiguration.derivedDataDir
                    .walkTopDown()
                    .first { it.extension == "xctestrun" }
        }

    fun receive(remoteDir: String, localDir: File) {
        if (!localDir.isDirectory) {
            throw IllegalArgumentException("Expected a directory at ${localDir.toString()}")
        }

        val sourceString = "$hostname:$remoteDir"

        val rsync = rsyncBase
                .source(sourceString)
                .destination(localDir.absolutePath.withTrailingFileSeparator())

        val output = CollectingProcessOutput()
        output.monitor(rsync.builder())
    }

    fun send(localDir: File, remoteDir: String) {
        if (!localDir.isDirectory) {
            throw IllegalArgumentException("Expected a directory at ${localDir.toString()}")
        }

        val destinationString = "$hostname:$remoteDir"

        val rsync = rsyncBase
                .source(localDir.absolutePath.withTrailingFileSeparator())
                .destination(destinationString)

        val output = CollectingProcessOutput()
        output.monitor(rsync.builder())
    }

    private val sshString: String
        get() {
            return "ssh -o 'UpdateHostKeys yes' -o 'StrictHostKeyChecking no' -F /dev/null " +
                "${if (configuration.debug) "-vvv" else ""} " +
                "-i ${iosConfiguration.remotePublicKey} " +
                "-l ${iosConfiguration.remoteUsername} " +
                "-p ${sshPort}"
        }

    private val rsyncBase: RSync
        get() {
            return RSync()
                    .a()
                    .partial(true)
                    .delete(true)
                    .rsh(sshString)
                    .verbose(configuration.debug)
        }
}

private fun String.withTrailingFileSeparator(): String {
    return this.dropLastWhile { it == File.separatorChar } + File.separatorChar
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
