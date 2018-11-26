package com.malinskiy.marathon.test

import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.Configuration
import kotlinx.coroutines.experimental.channels.Channel
import org.amshove.kluent.When
import org.amshove.kluent.`it returns`
import org.amshove.kluent.calling

class MarathonFactory {
    var configuration: Configuration = Mocks.Configuration.DEFAULT
    var tests: List<Test>
        set(value) {
            val testParser = configuration.vendorConfiguration.testParser()!!
            When calling testParser.extract(configuration) `it returns` (value)
        }
        get() = TODO("Not implemented")

    fun provideDevices(f: suspend (Channel<DeviceProvider.DeviceEvent>) -> Unit) {
        val stubDeviceProvider = configuration.vendorConfiguration.deviceProvider() as StubDeviceProvider
        stubDeviceProvider.providingLogic = f
    }

    fun build() = Marathon(configuration)
}