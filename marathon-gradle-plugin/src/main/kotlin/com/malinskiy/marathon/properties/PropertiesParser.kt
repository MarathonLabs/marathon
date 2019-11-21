package com.malinskiy.marathon.properties

import org.gradle.api.Project

private const val PROPERTY_WORKER = "marathon.worker.enabled"

internal val Project.marathonProperties: MarathonProperties
    get() = MarathonProperties(
        isCommonWorkerEnabled = findProperty(PROPERTY_WORKER)?.toString()?.toBoolean() ?: false
    )
