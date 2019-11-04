package com.malinskiy.marathon

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import org.gradle.api.DefaultTask

abstract class BaseMarathonRunTask : DefaultTask() {

    protected val log = MarathonLogging.logger {}

    lateinit var configuration: Configuration

}
