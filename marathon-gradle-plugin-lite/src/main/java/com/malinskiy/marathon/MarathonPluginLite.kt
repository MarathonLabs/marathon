package com.malinskiy.marathon

import com.malinskiy.marathon.config.Config
import com.malinskiy.marathon.config.ConfigGenerator
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class MarathonPluginLite : Plugin<Project> {
    private val downloader = Downloader()
    private val configGenerator = ConfigGenerator()
    val VERSION = "0.6.0"
    val DEFAULT_URL_TEMPLATE = "https://github.com/Malinskiy/marathon/releases/download/%s/marathon-%s.zip"
    val zipFileName = "marathon.zip";

    override fun apply(project: Project) {
        val url = String.format(DEFAULT_URL_TEMPLATE, VERSION, VERSION)
        val buildDir = project.buildDir
        downloadMarathonBinary(url, buildDir)
//        generateConfig()
    }

    private fun downloadMarathonBinary(url: String, outputDir: File) {
        val zipFile = Paths.get(outputDir.path, zipFileName).toString()
        downloader.download(url, zipFile)
        val process = ProcessBuilder()
            .command("unzip", "marathon.zip")
            .inheritIO()
            .start()
        process.waitFor()
    }

    private fun generateConfig(config: Config) {
        val marathonConfig = Files.createTempFile("marathon", "MarathonFile")
        configGenerator.saveConfig(config, marathonConfig.toFile())
    }
}
