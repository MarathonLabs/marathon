package com.malinskiy.marathon.gradle.task

import com.malinskiy.marathon.gradle.Const
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.Sync
import javax.inject.Inject

@CacheableTask
open class MarathonUnpackTask @Inject constructor(objects: ObjectFactory) : Sync() {
    init {
        group = Const.GROUP
    }
    
    @OutputDirectory
    val dist: DirectoryProperty = objects.directoryProperty()

    companion object {
        const val NAME = "marathonWrapper"
    }
}
