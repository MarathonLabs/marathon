package com.malinskiy.marathon.exceptions

import com.bugsnag.Bugsnag
import com.malinskiy.marathon.BuildConfig
import java.util.*

class BugsnagExceptionsReporter : ExceptionsReporter {
    private val bugsnag: Bugsnag by lazy {
        val bytes = Base64.getDecoder().decode(BuildConfig.BUGSNAG_TOKEN)
        Bugsnag(String(bytes))
    }

    override fun start(appType: AppType) {
        bugsnag.setAppType(appType.value)
        bugsnag.setAppVersion(BuildConfig.VERSION)
    }
}

enum class AppType(val value: String) {
    CLI("cli"),
    GRADLE_PLUGIN("gradle-plugin")
}