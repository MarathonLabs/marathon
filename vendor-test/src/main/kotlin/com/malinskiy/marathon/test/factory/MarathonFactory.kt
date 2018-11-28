package com.malinskiy.marathon.test.factory

import com.malinskiy.marathon.Marathon

class MarathonFactory {
    val configurationFactory: ConfigurationFactory = ConfigurationFactory()

    fun configuration(block: ConfigurationFactory.() -> Unit) = configurationFactory.apply(block)

    fun build() = Marathon(configurationFactory.build())
}