package com.malinskiy.marathon.android

import com.malinskiy.marathon.io.FileHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Extracts digest of APK contents from signature file
 */
class ApkFileHasher : FileHasher {

    override suspend fun getHash(file: File): String = withContext(Dispatchers.IO) {
        val zipFile = Paths.get(file.absolutePath)

        FileSystems.newFileSystem(zipFile, null)
            .use { fileSystem ->
                val certFile = fileSystem.getPath(SIGNATURE_FILE_PATH)
                Files.newInputStream(certFile)
                    .use {
                        it
                            .bufferedReader()
                            .lineSequence()
                            .firstOrNull { line -> line.contains(DIGEST_MANIFEST_PROPERTY) }
                            ?.substringAfter(PROPERTY_DELIMITER)
                            ?.trim()
                            ?: throw IllegalArgumentException("Manifest digest not found")
                    }
            }
    }

    private companion object {
        private const val SIGNATURE_FILE_PATH = "META-INF/CERT.SF"
        private const val DIGEST_MANIFEST_PROPERTY = "-Digest-Manifest: "
        private const val PROPERTY_DELIMITER = ": "
    }
}