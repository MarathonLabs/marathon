package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.request.device.Device
import com.malinskiy.adam.request.device.DeviceState
import java.util.concurrent.ConcurrentHashMap

class DeviceStateTracker {
    private val devices: ConcurrentHashMap<String, DeviceState> = ConcurrentHashMap()

    fun update(updatedDevices: List<Device>): List<Pair<String, TrackingUpdate>> {
        val updates = mutableListOf<Pair<String, TrackingUpdate>>()

        val devicesNotReportedAnymore = devices.keys - updatedDevices.map { it.serial }
        devicesNotReportedAnymore.forEach { serial ->
            when (devices[serial]) {
                DeviceState.DEVICE -> {
                    updates.add(Pair(serial, TrackingUpdate.DISCONNECTED))
                    devices.remove(serial)
                }
            }
        }

        updatedDevices.forEach { device ->
            val previousState = this.devices[device.serial]
            val newState = device.state

            if (previousState == null) {
                updates.add(Pair(device.serial, processNewDevice(newState, device.serial)))
            } else {
                updates.add(Pair(device.serial, processDeviceUpdate(device.serial, previousState, newState)))
            }
        }

        return updates
    }

    private fun processDeviceUpdate(serial: String, previousState: DeviceState, newState: DeviceState): TrackingUpdate {
        devices[serial] = newState

        return when {
            previousState != DeviceState.DEVICE -> processNewDevice(newState, serial)
            previousState == DeviceState.DEVICE && newState != DeviceState.DEVICE -> return TrackingUpdate.DISCONNECTED
            else -> TrackingUpdate.NOTHING_TO_DO
        }
    }

    private fun processNewDevice(newState: DeviceState, serial: String): TrackingUpdate {
        devices[serial] = newState
        return when (newState) {
            DeviceState.DEVICE -> TrackingUpdate.CONNECTED
            else -> TrackingUpdate.NOTHING_TO_DO
        }
    }

    fun getState(adbSerial: String) = devices[adbSerial]
}

enum class TrackingUpdate {
    CONNECTED,
    DISCONNECTED,
    NOTHING_TO_DO
}
