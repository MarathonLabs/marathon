package com.malinskiy.marathon.config.serialization.yaml

import com.fasterxml.jackson.databind.module.SimpleModule
import com.malinskiy.marathon.config.AnalyticsConfiguration
import com.malinskiy.marathon.config.environment.EnvironmentReader
import com.malinskiy.marathon.config.serialization.time.InstantTimeProvider
import com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration
import com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import java.io.File

class SerializeModule(
    instantTimeProvider: InstantTimeProvider,
    environmentReader: EnvironmentReader,
    marathonfileDir: File,
    fileListProvider: FileListProvider
) :
    SimpleModule() {
    init {
//        addDeserializer(AnalyticsConfiguration::class.java, AnalyticsConfigurationDeserializer())
//        addSerializer(AnalyticsConfiguration::class.java, AnalyticsConfigurationSerializer())

        addDeserializer(AnalyticsConfiguration.InfluxDbConfiguration::class.java, InfluxDbConfigurationDeserializer())
        addDeserializer(AnalyticsConfiguration.GraphiteConfiguration::class.java, GraphiteConfigurationDeserializer())
        addDeserializer(
            AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration::class.java,
            RetentionPolicyConfigurationDeserializer()
        )

        addDeserializer(
            SortingStrategyConfiguration.ExecutionTimeSortingStrategyConfiguration::class.java,
            ExecutionTimeSortingStrategyConfigurationDeserializer(instantTimeProvider)
        )

        addDeserializer(
            FlakinessStrategyConfiguration.ProbabilityBasedFlakinessStrategyConfiguration::class.java,
            ProbabilityBasedFlakinessStrategyConfigurationDeserializer(instantTimeProvider)
        )
        addDeserializer(
            BatchingStrategyConfiguration.FixedSizeBatchingStrategyConfiguration::class.java,
            FixedSizeBatchingStrategyConfigurationDeserializer(instantTimeProvider)
        )

        addSerializer(Regex::class.java, RegexSerializer())

        addDeserializer(File::class.java, FileDeserializer(marathonfileDir))
//        addSerializer(VendorConfiguration::class.java, VendorConfigurationSerializer())
        addDeserializer(
            VendorConfiguration::class.java,
            VendorConfigurationDeserializer(marathonfileDir, environmentReader, fileListProvider)
        )
    }
}
