package com.malinskiy.marathon.exceptions

import com.bugsnag.Bugsnag
import com.malinskiy.marathon.BuildConfig
import java.util.*

class BugsnagExceptionsReporter : ExceptionsReporter {
    private val bugsnag: Bugsnag? by lazy {
        val bugsnagToken = BuildConfig.BUGSNAG_TOKEN
        if (bugsnagToken.isNullOrEmpty()) {
            null
        } else {
            val bytes = Base64.getDecoder().decode(bugsnagToken)
            Bugsnag(String(bytes))
        }
    }

    override fun start(appType: AppType) {
        bugsnag?.apply {
            setAppType(appType.value)
            setAppVersion(BuildConfig.VERSION)
        }
    }
}

enum class AppType(val value: String) {
    CLI("cli"),
    GRADLE_PLUGIN("gradle-plugin")
}