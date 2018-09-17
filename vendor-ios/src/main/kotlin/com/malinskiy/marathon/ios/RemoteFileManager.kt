package com.malinskiy.marathon.ios

import com.malinskiy.marathon.test.Test

object RemoteFileManager {

    private const val OUTPUT_DIR = "/tmp/"

    fun remoteVideoForTest(test: Test): String {
        return remoteFileForTest(videoFileName(test))
    }

    private fun remoteFileForTest(filename: String): String {
        return "$OUTPUT_DIR/$filename"
    }

    private fun videoFileName(test: Test): String = "${test.pkg}-${test.clazz}-${test.method}.mp4"
}
