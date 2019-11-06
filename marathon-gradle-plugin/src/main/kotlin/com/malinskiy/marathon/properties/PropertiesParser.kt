package com.malinskiy.marathon.properties

import org.gradle.api.Project

private const val PROPERTY_WORKER = "marathon.worker.enabled"
private const val PROPERTY_WORKER_AUTO_START = "marathon.worker.autostart.enabled"

internal val Project.marathonProperties: MarathonProperties
    get() = MarathonProperties(
        isCommonWorkerEnabled = findProperty(PROPERTY_WORKER)?.toString()?.toBoolean() ?: false,
        isWorkerAutoStartEnabled = findProperty(PROPERTY_WORKER_AUTO_START)?.toString()?.toBoolean() ?: true
    )
