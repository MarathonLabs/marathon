package com.malinskiy.marathon.di

import com.malinskiy.marathon.analytics.internal.pub.Track
import org.koin.dsl.module

val analyticsModule = module {
    single { Track() }
}