package com.malinskiy.marathon.di

import com.google.gson.Gson
import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.analytics.TrackerFactory
import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.external.AnalyticsFactory
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.analytics.internal.sub.TrackerInternal
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.time.SystemTimer
import com.malinskiy.marathon.time.Timer
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.definition.DefinitionFactory
import org.koin.dsl.module
import java.time.Clock

val analyticsModule = module {
    single<Track> { Track() }
    single<TrackerInternal> { TrackerFactory(get(), get(), get(), get(), get()).create() }
    single<Analytics> { AnalyticsFactory(get()).create() }
}

val coreModule = module {
    single<FileManager> { FileManager(get<Configuration>().outputDir) }
    single<Gson> { Gson() }
    single<Clock> { Clock.systemDefaultZone() }
    single<Timer> { SystemTimer(get()) }
    single<ProgressReporter> { ProgressReporter(get()) }
    single<Marathon> { Marathon(get(), get(), get(), get(), get(), get()) }
}

fun KoinApplication.marathonConfiguration(configuration: Configuration): KoinApplication {
    koin.rootScope.beanRegistry.saveDefinition(DefinitionFactory.createSingle { configuration })
    return this
}

fun marathonStartKoin(configuration: Configuration): KoinApplication {
    return startKoin {
        marathonConfiguration(configuration)
        modules(coreModule)
        modules(analyticsModule)
        modules(configuration.vendorConfiguration.modules())
    }
}
