package com.malinskiy.marathon.di

import com.google.gson.GsonBuilder
import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.analytics.TrackerFactory
import com.malinskiy.marathon.analytics.external.AnalyticsFactory
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.execution.command.parse.MarathonTestParseCommand
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.json.FileSerializer
import com.malinskiy.marathon.time.SystemTimer
import com.malinskiy.marathon.time.Timer
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File
import java.time.Clock

val analyticsModule = module {
    single { Track() }
    single { TrackerFactory(get(), get(), get(), get(), get()).create() }
    single { AnalyticsFactory(get()).create() }
}

val coreModule = module {
    single {
        val configuration = get<Configuration>()
        FileManager(configuration.outputConfiguration.maxPath, configuration.outputDir) 
    }
    single {
        GsonBuilder()
            .registerTypeAdapter(File::class.java, FileSerializer())
            .create()
    }
    single<Clock> { Clock.systemDefaultZone() }
    single<Timer> { SystemTimer(get()) }
    single {
        val configuration = get<Configuration>()
        MarathonTestParseCommand(configuration.outputDir)
    }
    single { Marathon(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

fun marathonStartKoin(configuration: Configuration, modules: List<Module>): KoinApplication {
    val configurationModule = module {
        single { configuration }
    }

    return startKoin {
        modules(configurationModule)
        modules(coreModule)
        modules(analyticsModule)
        modules(modules)
    }
}
