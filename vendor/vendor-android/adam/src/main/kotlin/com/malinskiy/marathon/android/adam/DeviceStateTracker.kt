package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.request.devices.DeviceState
import java.util.concurrent.ConcurrentHashMap

class DeviceStateTracker {
    private val devices: ConcurrentHashMap<String, DeviceState> = ConcurrentHashMap()

    fun update(serial: String, newState: DeviceState): TrackingUpdate {
        val previousState = devices[serial]

        return if (previousState == null) {
            processNewDevice(newState, serial)
        } else {
            processDeviceUpdate(serial, previousState, newState)
        }
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
