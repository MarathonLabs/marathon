package com.malinskiy.marathon.config.vendor.apple

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.config.exceptions.ConfigurationException
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
    @JsonProperty("extraApplications") val extraApplications: List<File>? = null,
    @JsonProperty("derivedDataDir") val derivedDataDir: File? = null,
    @JsonProperty("testType") val testType: TestType? = null,
    private val tempDirFor: (File) -> File = { file ->
        File(file.parentFile, file.nameWithoutExtension).apply { deleteRecursively(); mkdirs() }
    }
) {
    @JsonIgnore
    var app: File? = null

    @JsonIgnore
    var testApp: File? = null

    @JsonIgnore
    lateinit var xctest: File

    fun validate() {
        when {
            application != null && testApplication != null -> {
                app = when {
                    application.isFile && setOf("ipa", "zip").contains(application.extension) -> {
                        extractAndFindDirectory(application, "app", validate = true)
                    }

                    application.isDirectory && (application.extension == "app") -> {
                        application
                    }

                    else -> throw ConfigurationException("application should be .ipa/.zip archive or a .app folder")
                }
                when {
                    testApplication.isFile && setOf("ipa", "zip").contains(testApplication.extension) -> {
                        val extracted = extract(testApplication)
                        val possibleTestApp = findDirectoryInDirectory(extracted, "app", validate = false)
                        if (possibleTestApp != null) {
                            testApp = possibleTestApp
                            xctest = findDirectoryInDirectory(possibleTestApp, "xctest", validate = true)
                                ?: throw ConfigurationException("Unable to find xctest bundle")
                        } else {
                            xctest = findDirectoryInDirectory(extracted, "xctest", validate = true)
                                ?: throw ConfigurationException("Unable to find xctest bundle")
                        }
                    }

                    testApplication.isDirectory && testApplication.extension == "app" -> {
                        testApp = testApplication
                        xctest = findDirectoryInDirectory(testApplication, "xctest", validate = true)
                            ?: throw ConfigurationException("Unable to find xctest bundle")
                    }

                    testApplication.isDirectory && testApplication.extension == "xctest" -> {
                        xctest = testApplication
                    }

                    else -> throw ConfigurationException("test application should be .ipa/.zip archive or a .app/.xctest folder")
                }
            }

            derivedDataDir != null -> {
                xctest =
                    findDirectoryInDirectory(derivedDataDir, "xctest", true) ?: throw ConfigurationException("Unable to find xctest bundle")
                app = findDirectoryInDirectory(derivedDataDir, "app", true)
            }

            else -> throw ConfigurationException("please specify your application and test application either with files or provide derived data folder")
        }
    }

    private fun findDirectoryInDirectory(directory: File, extension: String, validate: Boolean): File? {
        var found = mutableListOf<File>()
        directory.walkTopDown().forEach {
            if (it.isDirectory && it.extension == extension) {
                found.add(it)
            }
        }
        when {
            found.isEmpty() && validate -> throw ConfigurationException("Unable to find an $extension directory in ${directory.absolutePath}")
            found.size == 2 -> {
                //According to https://developer.apple.com/documentation/bundleresources/placing_content_in_a_bundle watch app will be placed
                //under X.app/Watch/Y.app
                //Consider only the X.app for such case

                val a = found.removeFirst()
                val b = found.removeFirst()
                return if (a.relativeTo(b).toPath().firstOrNull()?.toString() == "Watch") {
                    b
                } else if (b.relativeTo(a).toPath().firstOrNull()?.toString() == "Watch") {
                    a
                } else {
                    throw ConfigurationException("Ambiguous $extension configuration in derived data folder [${found.joinToString { it.absolutePath }}]. Please specify parameters explicitly")
                }
            }

            found.size > 2 -> throw ConfigurationException("Ambiguous $extension configuration in derived data folder [${found.joinToString { it.absolutePath }}]. Please specify parameters explicitly")
        }
        return found.firstOrNull()
    }

    private fun extractAndFindDirectory(file: File, extension: String, validate: Boolean): File? {
        val extracted = extract(file)
        return findDirectoryInDirectory(extracted, extension, validate)
    }

    private fun extract(file: File): File {
        val dst = tempDirFor(file)
        ZipFile(file).use { zipFile ->
            for (entry in zipFile.entries()) {
                when {
                    IGNORED.contains(entry.name) -> {
                        continue
                    }

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

    companion object {
        //See https://en.wikipedia.org/wiki/Resource_fork
        val IGNORED = setOf("__MACOSX", ".DS_Store")
    }
}

