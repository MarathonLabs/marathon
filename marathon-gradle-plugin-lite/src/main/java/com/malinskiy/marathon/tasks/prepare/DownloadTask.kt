package com.malinskiy.marathon.tasks.prepare

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels

open class DownloadTask(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : DefaultTask() {
    @get:Input
    internal val url: Property<String> = objects.property()

    @get:Input
    internal val destination: Property<String> = objects.property()

    @OutputFile
    val marathonBinDir: Property<Directory> = objects.directoryProperty()

    @TaskAction
    fun greet() {
        val file = File(destination.get())
        file.parentFile.mkdirs()
        download(url.get(), file.path)
        marathonBinDir.set()
    }

    fun download(fileUrl: String, filePath: String) {
        val url = URL(fileUrl)
        val readableByteChannel = Channels.newChannel(url.openStream())
        val fileOutputStream = FileOutputStream(filePath)
        fileOutputStream.channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
    }
}
