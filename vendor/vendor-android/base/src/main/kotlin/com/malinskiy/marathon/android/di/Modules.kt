package com.malinskiy.marathon.android.di

import com.malinskiy.marathon.android.AndroidTestParser
import com.malinskiy.marathon.execution.TestParser
import org.koin.dsl.module

val androidModule = module {
    single<TestParser?> { AndroidTestParser() }
}