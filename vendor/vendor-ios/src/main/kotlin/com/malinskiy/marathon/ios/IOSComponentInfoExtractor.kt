package com.malinskiy.marathon.ios

import com.malinskiy.marathon.execution.ComponentInfo
import com.malinskiy.marathon.execution.ComponentInfoExtractor
import com.malinskiy.marathon.execution.Configuration

class IOSComponentInfoExtractor : ComponentInfoExtractor {

    override fun extract(configuration: Configuration): ComponentInfo {
        val iosConfiguration = configuration.vendorConfiguration as IOSConfiguration

        return IOSComponentInfo(
            iosConfiguration.xctestrunPath,
            iosConfiguration.derivedDataDir,
            iosConfiguration.sourceRoot,
            configuration.name
        )
    }
}
