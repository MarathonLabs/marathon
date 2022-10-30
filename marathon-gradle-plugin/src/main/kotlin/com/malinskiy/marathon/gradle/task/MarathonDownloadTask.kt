package com.malinskiy.marathon.gradle.task

import com.malinskiy.marathon.gradle.Const
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.OutputFile
import javax.inject.Inject

open class MarathonDownloadTask @Inject constructor(objects: ObjectFactory) : Copy() {
    init {
        group = Const.GROUP
    }
    
    @OutputFile
    val distZip: RegularFileProperty = objects.fileProperty()

    companion object {
        const val NAME = "marathonWrapperExtract"
    }
}
