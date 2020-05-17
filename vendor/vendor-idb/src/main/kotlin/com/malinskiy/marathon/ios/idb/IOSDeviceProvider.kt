package com.malinskiy.marathon.ios.idb

import com.google.gson.Gson
import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.vendor.VendorConfiguration
import idb.TargetDescriptionRequest
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.channels.Channel

class IOSDeviceProvider : DeviceProvider {
    override val deviceInitializationTimeoutMillis: Long = 0L

    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()

    override suspend fun initialize(vendorConfiguration: VendorConfiguration) {
        if (vendorConfiguration !is IdbConfiguration) {
            throw IllegalStateException("Invalid configuration $vendorConfiguration")
        }
        val gson = Gson()
        val reader = IdbHostsReader(gson)
        val configPath = vendorConfiguration.idbHosts
        val hosts = reader.readConfig(configPath)
        hosts.forEach {
            val grpcChannel = ManagedChannelBuilder.forAddress(it.host, it.port).build()
            val stub = idb.CompanionServiceGrpcKt.CompanionServiceCoroutineStub(grpcChannel)
            val client = IdbClient(grpcChannel, stub)
            val description = client.describe()
            channel.send(DeviceProvider.DeviceEvent.DeviceConnected(IOSDevice(client, description)))
        }
    }

    override suspend fun terminate() {
    }

    override fun subscribe(): Channel<DeviceProvider.DeviceEvent> {
        return channel
    }
}
