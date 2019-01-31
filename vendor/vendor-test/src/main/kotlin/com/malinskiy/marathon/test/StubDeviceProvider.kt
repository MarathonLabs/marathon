package com.malinskiy.marathon.test

import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class StubDeviceProvider : DeviceProvider, CoroutineScope {
    lateinit var context: CoroutineContext

    override val coroutineContext: kotlin.coroutines.CoroutineContext
        get() = context

    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()
    var providingLogic: (suspend (Channel<DeviceProvider.DeviceEvent>) -> Unit)? = null

    override val deviceInitializationTimeoutMillis: Long = 180_000
    override suspend fun initialize(vendorConfiguration: VendorConfiguration) {
    }

    override fun subscribe(): Channel<DeviceProvider.DeviceEvent> {
        providingLogic?.let {
            launch(context = coroutineContext) {
                providingLogic?.invoke(channel)
            }
        }

        return channel
    }

    override suspend fun terminate() {
        channel.close()
    }
}
