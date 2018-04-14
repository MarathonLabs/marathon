package com.malinskiy.marathon.android

import com.android.ddmlib.IDevice
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.test.TestBatch

class AndroidDevice(val ddmsDevice: IDevice) : Device {
    override val model: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val manufacturer: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val deviceFeatures: Collection<DeviceFeature>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val serialNumber: String
        get() = ddmsDevice.serialNumber ?: "unknown"
    override val operatingSystem: OperatingSystem
        get() = OperatingSystem(ddmsDevice.version?.apiString ?: "unknown")
    override val networkState: NetworkState
        get() = when(ddmsDevice.isOnline) {
            true -> NetworkState.CONNECTED
            else -> NetworkState.DISCONNECTED
        }
    override val healthy: Boolean
        get() = when(ddmsDevice.state) {
            IDevice.DeviceState.ONLINE -> true
            else -> false
        }

    override fun execute(testBatch: TestBatch) {
        TODO("not implemented")
    }
}