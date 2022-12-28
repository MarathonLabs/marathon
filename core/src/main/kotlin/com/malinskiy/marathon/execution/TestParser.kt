package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.test.Test

interface TestParser

interface LocalTestParser : TestParser {
    suspend fun extract(): List<Test>
}

interface RemoteTestParser<in T : DeviceProvider> : TestParser {
    /**
     * @param some test frameworks only generate the list of tests during runtime, so parsing bytecode is not feasible
     *      for those situations the device provider will be provided
     */
    suspend fun extract(deviceProvider: DeviceProvider): List<Test>
}
