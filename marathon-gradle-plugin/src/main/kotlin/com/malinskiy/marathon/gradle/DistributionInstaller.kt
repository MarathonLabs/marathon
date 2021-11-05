package com.malinskiy.marathon.gradle

import org.apache.commons.codec.digest.DigestUtils
import org.gradle.internal.os.OperatingSystem
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import java.util.zip.ZipFile

class DistributionInstaller {
    fun install(marathonBuildDir: File): Pair<File, File> {
        val marathonZip = copyFromResources(marathonBuildDir)
        unzip(marathonZip, marathonBuildDir)
        return Pair(marathonBuildDir, getPlatformScript(marathonBuildDir))
    }

    private fun getPlatformScript(marathonBuildDir: File) = when (OperatingSystem.current()) {
        OperatingSystem.WINDOWS -> {
            Paths.get(marathonBuildDir.canonicalPath, "cli", "bin", "marathon.bat").toFile()
        }
        else -> {
            val cliPath = Paths.get(marathonBuildDir.canonicalPath, "cli", "bin", "marathon")
            cliPath.apply {
                val permissions = Files.getPosixFilePermissions(this)
                Files.setPosixFilePermissions(this, permissions + PosixFilePermission.OWNER_EXECUTE)
            }.toFile()
        }
    }

    private fun copyFromResources(marathonBuildDir: File): File {
        val marathonZip = File(marathonBuildDir, "marathon-cli.zip")
        if (!marathonZip.exists() || !compareDist(marathonZip)) {
            marathonZip.outputStream().buffered().use {
                MarathonPlugin::class.java.getResourceAsStream(CLI_PATH).copyTo(it)
            }
        }
        return marathonZip
    }

    private fun unzip(marathonZip: File, marathonBuildDir: File) {
        marathonBuildDir.listFiles()?.forEach {
            if (it.isDirectory) {
                it.deleteRecursively()
            }
        }
        File(marathonBuildDir, "cli").delete()
        ZipFile(marathonZip).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    val filePath = marathonBuildDir.canonicalPath + File.separator + entry.name
                    val file = File(filePath)
                    if (!entry.isDirectory) {
                        file.parentFile.mkdirs()
                        file.outputStream().buffered().use {
                            input.copyTo(it)
                        }
                    } else {
                        file.mkdirs()
                    }
                }
            }
        }
        marathonBuildDir.listFiles()?.forEach {
            if (it.isDirectory) {
                it.renameTo(File(it.parent, "cli"))
            }
        }
    }

    private fun compareDist(file: File): Boolean {
        val bundledMd5 = MarathonPlugin::class.java.getResourceAsStream(CLI_PATH).use {
            DigestUtils.md5Hex(it)
        }

        val installedMd5 = file.inputStream().buffered().use {
            DigestUtils.md5Hex(it)
        }

        return bundledMd5 == installedMd5
    }

    companion object {
        const val CLI_PATH = "/marathon-cli.zip"
    }
}
