package com.malinskiy.marathon.config.vendor.ios

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.serialization.yaml.FileListProvider
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile
import kotlin.io.path.Path


/**
 * Represent the multitude of options on how one can distribute the application and test application in Apple ecosystem
 */
data class AppleTestBundleConfiguration(
    @JsonProperty("application") val application: File? = null,
    @JsonProperty("testApplication") val testApplication: File? = null,
    @JsonProperty("derivedDataDir") val derivedDataDir: File? = null,
    @JsonProperty("testType") val testType: TestType? = null,
    private val tempDirFor: (File) -> File = { file ->
        File(file.parentFile, file.nameWithoutExtension).apply { deleteRecursively(); mkdirs() }
    }
) {
    @JsonIgnore var app: File? = null
    @JsonIgnore lateinit var xctest: File

    fun validate() {
        when {
            application != null && testApplication != null -> {
                app = when {
                    application.isFile && setOf("ipa", "zip").contains(application.extension) -> {
                        extractAndValidateContainsDirectory(application, "app")
                    }

                    application.isDirectory && (application.extension == "app") -> {
                        application
                    }

                    else -> throw ConfigurationException("application should be .ipa/.zip archive or a .app folder")
                }
                xctest = when {
                    testApplication.isFile && setOf("ipa", "zip").contains(testApplication.extension) -> {
                        extractAndValidateContainsDirectory(testApplication, "xctest")
                    }

                    testApplication.isDirectory && testApplication.extension == "xctest" -> {
                        testApplication
                    }

                    else -> throw ConfigurationException("test application should be .ipa/.zip archive or a .xctest folder")
                }
            }

            derivedDataDir != null -> {
                xctest = findDirectoryInDirectory(derivedDataDir, "xctest")
                app = findDirectoryInDirectory(derivedDataDir, "app")
            }

            else -> throw ConfigurationException("please specify your application and test application either with files or provide derived data folder")
        }
    }

    private fun findDirectoryInDirectory(directory: File, extension: String): File {
        var found = mutableListOf<File>()
        directory.walkTopDown().forEach {
            if (it.isDirectory && it.extension == extension) {
                found.add(it)
            }
        }
        when {
            found.isEmpty() -> throw ConfigurationException("Unable to find an $extension directory in ${directory.absolutePath}")
            found.size > 1 -> throw ConfigurationException("Ambiguous $extension configuration in derived data folder [${found.joinToString { it.absolutePath }}]. Please specify parameters explicitly")
        }
        return found.first()
    }

    private fun extractAndValidateContainsDirectory(file: File, extension: String): File {
        val extracted = extract(file)
        return findDirectoryInDirectory(extracted, extension)
    }

    private fun extract(file: File): File {
        val dst = tempDirFor(file)
        ZipFile(file).use { zipFile ->
            for (entry in zipFile.entries()) {
                when {
                    entry.isDirectory -> {
                        var subpath = entry.name.split("/").filter { it.trim().isNotEmpty() }
                        Path(dst.path, *subpath.toTypedArray()).toFile().mkdirs()
                    }

                    else -> {
                        val fileName = entry.name.substringAfterLast("/")
                        val parent = entry.name.substringBeforeLast("/").split("/").filter { it.trim().isNotEmpty() }
                        val parentFile = Path(dst.path, *parent.toTypedArray()).toFile().apply { mkdirs() }
                        FileOutputStream(File(parentFile, fileName)).use { outStream ->
                            zipFile.getInputStream(entry).use { inStream ->
                                inStream.copyTo(outStream)
                            }
                        }
                    }
                }
            }
        }
        return dst
    }
}

