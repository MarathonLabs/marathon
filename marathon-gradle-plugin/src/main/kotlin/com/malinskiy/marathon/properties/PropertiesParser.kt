package com.malinskiy.marathon.properties

import org.gradle.api.Project

private const val PROPERTY_WORKER = "marathon.worker.enabled"
private const val PROPERTY_WORKER_AUTO_START = "marathon.worker.autostart.enabled"

fun getProperties(project: Project): MarathonProperties =
    MarathonProperties(
        isCommonWorkerEnabled = project.findProperty(PROPERTY_WORKER)?.toString()?.toBoolean() ?: false,
        isWorkerAutoStartEnabled = project.findProperty(PROPERTY_WORKER_AUTO_START)?.toString()?.toBoolean() ?: true
    )
