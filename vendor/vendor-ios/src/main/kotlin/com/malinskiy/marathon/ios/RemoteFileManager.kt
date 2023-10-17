package com.malinskiy.marathon.ios

import com.malinskiy.marathon.config.vendor.ios.Type
import com.malinskiy.marathon.extension.escape
import com.malinskiy.marathon.ios.RemoteFileManager.Companion.FILE_SEPARATOR
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toClassName

/**
 * We deliberately don't use File here since File depends on system file separator
 */
class RemoteFileManager(private val device: AppleDevice) {
    private val logger = MarathonLogging.logger {}
    private val outputDir by lazy { device.storagePath }

    fun remoteDirectory(): String = outputDir

    suspend fun createRemoteDirectory(remoteDir: String = remoteDirectory()) {
        executeCommand(
            listOf("mkdir", "-p", remoteDir),
            "Could not create remote directory $remoteDir"
        )
    }

    suspend fun removeRemoteDirectory() {
        executeCommand(
            listOf("rm", "-rf", remoteDirectory()),
            "Unable to remove directory ${remoteDirectory()}"
        )
    }

    suspend fun removeRemotePath(path: String) {
        executeCommand(
            listOf("rm", "-rf", path),
            "Unable to remove path $path"
        )
    }

    fun remoteXctestrunFile(): String = remoteFile(xctestrunFileName())

    fun remoteXctestFile(): String = remoteFile(xctestFileName())
    fun remoteXctestParserFile(): String = remoteFile(`libXctestParserFileName`())
    fun remoteApplication(): String = remoteFile(appUnderTestFileName())
    fun remoteExtraApplication(name: String) = remoteFile(name)

    /**
     * Omitting xcresult extension results in a symlink 
     */
    fun remoteXcresultFile(batch: TestBatch): String = remoteFile(xcresultFileName(batch))

    fun xctestrunFileName(): String = "marathon.xctestrun"

    private fun xctestFileName(): String  = "marathon.xctest"
    private fun libXctestParserFileName(): String  = "libxctest-parser.dylib"

    fun appUnderTestFileName(): String  = "appUnderTest.app"
    
    private fun xcresultFileName(batch: TestBatch): String =
        "${device.udid}.${batch.id}.xcresult"

    private fun remoteFile(file: String): String = remoteDirectory().resolve(file)

    private suspend fun safeExecuteCommand(command: List<String>) {
        try {
            device.executeWorkerCommand(command)
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun executeCommand(command: List<String>, errorMessage: String): String? {
        return try {
            val result = device.executeWorkerCommand(command) ?: return null
            val stderr = result.combinedStderr.trim()
            if(stderr.isNotBlank()) {
                logger.error { "cmd=${command.joinToString(" ")}, stderr=$stderr" }
            }
            result.combinedStdout.trim()
        } catch (e: Exception) {
            logger.error(errorMessage, e)
            null
        }
    }

    fun remoteVideoForTest(test: Test, testBatchId: String): String {
        return remoteFileForTest(videoFileName(test, testBatchId))
    }

    fun remoteVideoPidfile() = remoteFileForTest(videoPidFileName(device.udid))

    fun remoteScreenshot(udid: String, type: Type): String {
        return remoteFileForTest(screenshotFileName(udid, type))
    }

    private fun remoteFileForTest(filename: String): String {
        return "${outputDir}$FILE_SEPARATOR$filename"
    }
    
    private fun screenshotFileName(udid: String, type: Type): String {
        return "$udid.${type.value}"
    }

    private fun videoFileName(test: Test, testBatchId: String): String {
        val testSuffix = "-$testBatchId.mp4"
        val testName = "${test.toClassName('-')}-${test.method}".escape()
        return "$testName$testSuffix"
    }

    fun parentOf(remoteXctestrunFile: String): String {
        return remoteXctestrunFile.substringBeforeLast(FILE_SEPARATOR)
    }

    private fun videoPidFileName(udid: String) = "${udid}.pid"
    fun remoteTestRoot() = remoteDirectory()
    
    fun joinPath(base: String, vararg args: String): String {
        return listOf(
            base.removeSuffix(FILE_SEPARATOR),
            *args
        ).joinToString(FILE_SEPARATOR)
    }

    suspend fun copy(src: String, dst: String, override: Boolean = true) {
        if(override) {
            safeExecuteCommand(
                listOf("rm", "-R", dst)
            )
        }
        executeCommand(
            listOf("cp", "-R", src, dst), "failed to copy remote directory $src to $dst"
        )
    }

    companion object {
        const val FILE_SEPARATOR = "/"
    }
}

/**
 * Adds relative file to this, considering this as a directory. If relative has a root, relative is returned back. 
 * For instance, "/foo/bar".resolve("gav") is "/foo/bar/gav". 
 * 
 * Returns:
 * concatenated this and relative paths, or just relative if it's absolute.
 */
private fun String.resolve(file: String): String {
    return when {
        file.startsWith(FILE_SEPARATOR) -> file
        else -> removeSuffix(FILE_SEPARATOR) + FILE_SEPARATOR + file.removeSuffix(FILE_SEPARATOR)
    }
}
