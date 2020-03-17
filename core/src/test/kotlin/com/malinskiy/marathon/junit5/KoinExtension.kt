package com.malinskiy.marathon.junit5

import com.malinskiy.marathon.di.analyticsModule
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class KoinExtension : BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext?) {
        startKoin {
            modules(analyticsModule)
        }
    }

    override fun afterEach(context: ExtensionContext?) {
        stopKoin()
    }

}
