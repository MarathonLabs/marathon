package com.malinskiy.marathon.di

import com.google.gson.Gson
import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.analytics.TrackerFactory
import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.external.AnalyticsFactory
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.analytics.internal.sub.TrackerInternal
import com.malinskiy.marathon.cache.CacheService
import com.malinskiy.marathon.cache.CacheServiceFactory
import com.malinskiy.marathon.cache.test.CacheTestReporter
import com.malinskiy.marathon.cache.test.CacheTestResultsTracker
import com.malinskiy.marathon.cache.test.TestCacheLoader
import com.malinskiy.marathon.cache.test.TestCacheSaver
import com.malinskiy.marathon.cache.test.TestResultsCache
import com.malinskiy.marathon.cache.test.key.TestCacheKeyFactory
import com.malinskiy.marathon.cache.test.key.VersionNameProvider
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.io.AttachmentManager
import com.malinskiy.marathon.io.CachedFileHasher
import com.malinskiy.marathon.io.FileHasher
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.Md5FileHasher
import com.malinskiy.marathon.time.SystemTimer
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.Dispatchers
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.definition.DefinitionFactory
import org.koin.dsl.module
import java.time.Clock

val analyticsModule = module {
    single<Track> { Track() }
    single<TrackerInternal> { TrackerFactory(get(), get(), get(), get(), get(), get(), get()).create() }
    single<Analytics> { AnalyticsFactory(get()).create() }
}

val cacheModule = module {
    single<CacheService> { CacheServiceFactory(get()).createCacheService() }
    single<TestCacheKeyFactory> { TestCacheKeyFactory(get(), get(), get()) }
    single<TestResultsCache> { TestResultsCache(get(), get()) }
    single<TestCacheLoader> { TestCacheLoader(get(), get(), get()) }
    single<CacheTestReporter> { CacheTestReporter(get(), get()) }
    single<CacheTestResultsTracker> { CacheTestResultsTracker(get()) }
    single<TestCacheSaver> { TestCacheSaver(get(), get()) }
    single<VersionNameProvider> { VersionNameProvider() }
}

val coreModule = module {
    single<FileManager> { FileManager(get<Configuration>().outputDir) }
    single<AttachmentManager> { AttachmentManager(get<Configuration>().outputDir) }
    single<FileHasher> { CachedFileHasher(Md5FileHasher()) }
    single<Gson> { Gson() }
    single<Clock> { Clock.systemDefaultZone() }
    single<Timer> { SystemTimer(get()) }
    single<ProgressReporter> { ProgressReporter() }
    single<Marathon> { Marathon(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

fun KoinApplication.marathonConfiguration(configuration: Configuration): KoinApplication {
    koin.rootScope.beanRegistry.saveDefinition(DefinitionFactory.createSingle { configuration })
    return this
}

fun marathonStartKoin(configuration: Configuration): KoinApplication {
    return startKoin {
        marathonConfiguration(configuration)
        modules(coreModule)
        modules(cacheModule)
        modules(analyticsModule)
        modules(configuration.vendorConfiguration.modules())
    }
}
