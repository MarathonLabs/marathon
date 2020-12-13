package com.malinskiy.marathon.exceptions

import com.malinskiy.marathon.config.AppType
import com.malinskiy.marathon.log.MarathonLogging

private val log = MarathonLogging.logger {}

class NoopExceptionsReporter : ExceptionsReporter {
    override fun start(appType: AppType) {
        log.info { "Noop exceptions reporter started" }
    }

    override fun end() {
        log.info { "Noop exceptions reporter finished" }
    }
}
