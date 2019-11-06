package com.malinskiy.marathon.test.factory

import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.di.marathonStartKoin

class MarathonFactory {
    private val configurationFactory: ConfigurationFactory = ConfigurationFactory()

    fun configuration(block: ConfigurationFactory.() -> Unit) = configurationFactory.apply(block)

    fun build(): Marathon {
        val marathonStartKoin = marathonStartKoin(configurationFactory.build())
        return marathonStartKoin.koin.get()
    }
}