package com.malinskiy.marathon.test.factory

import com.google.gson.Gson
import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.di.analyticsModule
import com.malinskiy.marathon.di.marathonConfiguration
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.time.SystemTimer
import com.malinskiy.marathon.time.Timer
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.time.Clock

class MarathonFactory {
    private val configurationFactory: ConfigurationFactory = ConfigurationFactory()

    var timer: Timer? = null

    fun configuration(block: ConfigurationFactory.() -> Unit) = configurationFactory.apply(block)

    fun build(): Marathon {
        val configuration = configurationFactory.build()

        val coreTestModule = module {
            single<FileManager> { FileManager(get<Configuration>().outputDir) }
            single<Gson> { Gson() }
            single<Clock> { Clock.systemDefaultZone() }
            single<Timer> { timer ?: SystemTimer(get()) }
            single<ProgressReporter> { ProgressReporter(get()) }
            single<Marathon> { Marathon(get(), get(), get(), get(), get(), get()) }
        }

        val marathonStartKoin = startKoin {
            marathonConfiguration(configuration)
            modules(coreTestModule)
            modules(analyticsModule)
            modules(configuration.vendorConfiguration.modules())
        }
        return marathonStartKoin.koin.get()
    }
}
