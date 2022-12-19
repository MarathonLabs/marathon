package com.malinskiy.marathon.device.file

import com.malinskiy.marathon.device.Device
import java.io.File
import kotlin.system.measureTimeMillis

inline fun <T : Any> Device.measureFileTransfer(file: File, block: () -> T): T {
    var result: T
    measureTimeMillis {
        result = block()
    }.let { time ->
        if(file.exists()) {
            val fileSize = file.length()
            val timeInSeconds = time.toDouble() / 1000
            if (timeInSeconds > .0f && fileSize > 0) {
                val speed = "%.2f".format((fileSize / 1000) / timeInSeconds)
                logger.debug {
                    "Transferred ${file.name} to/from $serialNumber. $speed KB/s ($fileSize bytes in ${
                        "%.4f".format(
                            timeInSeconds
                        )
                    })"
                }
            }
        }
    }
    return result
}

inline fun <T : Any> Device.measureFolderTransfer(file: File, block: () -> T): T {
    var result: T
    measureTimeMillis {
        result = block()
    }.let { time ->
        if(file.exists() && file.isDirectory) {
            var dirCount = 0
            var fileCount = 0
            var totalBytes = 0L
            file.walkTopDown().forEach { 
                when {
                    it.isDirectory -> dirCount++
                    it.isFile -> {
                        fileCount++
                        totalBytes += it.length()
                    }
                    else -> Unit
                }
            }
            val timeInSeconds = time.toDouble() / 1000
            if (timeInSeconds > .0f && totalBytes > 0) {
                val speed = "%.2f".format((totalBytes / 1000) / timeInSeconds)
                logger.debug {
                    "Transferred ${file.name} with $dirCount dirs and $fileCount files to/from $serialNumber. $speed KB/s ($totalBytes bytes in ${
                        "%.4f".format(
                            timeInSeconds
                        )
                    })"
                }
            }
        }
    }
    return result
}
