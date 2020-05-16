package com.malinskiy.marathon.ios.idb

import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.vendor.VendorConfiguration
import io.grpc.CallOptions
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.channels.Channel

class IOSDeviceProvider : DeviceProvider {
    override val deviceInitializationTimeoutMillis: Long = 0L

    override suspend fun initialize(vendorConfiguration: VendorConfiguration) {
        val channel = ManagedChannelBuilder.forAddress("localhost", 12345).build()
        val stub = idb.CompanionServiceGrpcKt.CompanionServiceCoroutineStub(channel)
    }

    override suspend fun terminate() {
    }

    override fun subscribe(): Channel<DeviceProvider.DeviceEvent> {
        return unboundedChannel()
    }
}
