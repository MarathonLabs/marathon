package com.malinskiy.marathon.test

import com.malinskiy.marathon.execution.ComponentInfo
import com.malinskiy.marathon.execution.ComponentInfoExtractor
import com.malinskiy.marathon.execution.Configuration

class StubComponentInfoExtractor : ComponentInfoExtractor {

    override fun extract(configuration: Configuration): ComponentInfo {
        return TestComponentInfo("test", configuration.outputDir)
    }

}
