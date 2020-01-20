package com.malinskiy.marathon.android.di

import com.malinskiy.marathon.android.AndroidComponentCacheKeyProvider
import com.malinskiy.marathon.android.AndroidComponentInfoExtractor
import com.malinskiy.marathon.android.AndroidTestParser
import com.malinskiy.marathon.android.ApkFileHasher
import com.malinskiy.marathon.cache.test.key.ComponentCacheKeyProvider
import com.malinskiy.marathon.execution.ComponentInfoExtractor
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.io.CachedFileHasher
import org.koin.dsl.module

val androidModule = module {
    single<TestParser?> { AndroidTestParser() }
    single<ComponentInfoExtractor?> { AndroidComponentInfoExtractor() }
    single<ComponentCacheKeyProvider?> { AndroidComponentCacheKeyProvider(CachedFileHasher(ApkFileHasher())) }
}
