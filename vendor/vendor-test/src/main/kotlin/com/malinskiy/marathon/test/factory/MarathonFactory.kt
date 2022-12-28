package com.malinskiy.marathon.test.factory

import com.google.gson.Gson
import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.di.analyticsModule
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.test.Mocks
import com.malinskiy.marathon.test.StubDeviceProvider
import com.malinskiy.marathon.time.SystemTimer
import com.malinskiy.marathon.time.Timer
import com.nhaarman.mockitokotlin2.mock
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.time.Clock

class MarathonFactory {
    private val testParser = Mocks.TestParser.DEFAULT
    private val deviceProvider = StubDeviceProvider()
    private val configurationFactory: ConfigurationFactory = ConfigurationFactory(testParser, deviceProvider)

    var timer: Timer? = null

    suspend fun configuration(block: suspend ConfigurationFactory.() -> Unit) {
        block(configurationFactory)
    }

    fun build(): Marathon {
        val configuration = configurationFactory.build()

        val coreTestModule = module {
            single {
                val configuration = get<Configuration>()
                FileManager(configuration.outputConfiguration.maxPath, configuration.outputDir)
            }
            single { Gson() }
            single<Clock> { Clock.systemDefaultZone() }
            single { timer ?: SystemTimer(get()) }
            single { ProgressReporter(get()) }
            single { Marathon(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
        }

        val configurationModule = module {
            single { configuration }
        }

        val marathonStartKoin = startKoin {
            modules(configurationModule)
            modules(coreTestModule)
            modules(analyticsModule)
            modules(module {
                single<TestParser> { testParser }
                factory<DeviceProvider> { deviceProvider }
                single<TestBundleIdentifier> { mock() }
                single<MarathonLogConfigurator> { mock() }
            })
        }
        return marathonStartKoin.koin.get()
    }
}
