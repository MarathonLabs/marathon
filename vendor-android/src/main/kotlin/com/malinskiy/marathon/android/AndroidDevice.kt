package com.malinskiy.marathon.android

import com.android.ddmlib.IDevice
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.test.Batch

class AndroidDevice(val ddmsDevice: IDevice) : Device {
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

    override fun execute(batch: Batch) {
        TODO("not implemented")
    }
}