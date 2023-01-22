package com.malinskiy.marathon.config

import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration
import com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration
import com.malinskiy.marathon.config.vendor.VendorConfiguration

class LogicalConfigurationValidator : ConfigurationValidator {
    override fun validate(configuration: Configuration) {
        when {
            configuration.flakinessStrategy !is FlakinessStrategyConfiguration.IgnoreFlakinessStrategyConfiguration &&
                configuration.shardingStrategy !is ShardingStrategyConfiguration.ParallelShardingStrategyConfiguration -> {
                throw ConfigurationException(
                    "Configuration is invalid: " +
                        "can't use complex sharding and complex flakiness strategy at the same time. " +
                        "See: https://github.com/MarathonLabs/marathon/issues/197"
                )
            }
        }

        configuration.filteringConfiguration.allowlist.forEach { it.validate() }
        configuration.filteringConfiguration.blocklist.forEach { it.validate() }
        
        when(configuration.vendorConfiguration) {
            is VendorConfiguration.IOSConfiguration -> {
                configuration.vendorConfiguration.validate()
            }

            else -> Unit
        }
    }
}
