package com.malinskiy.marathon.apple.ios.device

import com.malinskiy.marathon.apple.configuration.AppleTarget
import com.malinskiy.marathon.apple.configuration.Marathondevices
import com.malinskiy.marathon.apple.configuration.Transport
import com.malinskiy.marathon.apple.configuration.Worker
import com.malinskiy.marathon.log.MarathonLogging
import java.util.concurrent.ConcurrentHashMap

class DeviceTracker {
    private val workers: ConcurrentHashMap<String, WorkerTracker> = ConcurrentHashMap()

    fun update(marathondevices: Marathondevices): Map<Transport, List<TrackingUpdate>> {
        val updateWorkers = marathondevices.workers.map { it.id() }.toSet()

        val result = hashMapOf<Transport, List<TrackingUpdate>>()

        val workersNotReportedAnymore = workers.filterKeys { !updateWorkers.contains(it) }
        workersNotReportedAnymore.forEach { (id, tracker) ->
            val update = tracker.update(emptyList())
            result[tracker.transport] = update.map { it.second }
        }

        marathondevices.workers.forEach { worker ->
            val id = worker.id()
            val tracker = workers.getOrPut(id) { WorkerTracker(worker.transport) }
            val update = tracker.update(worker.devices).map { Pair(worker.transport, it.second) }
            result[tracker.transport] = update.map { it.second }
        }

        return result
    }

    private fun Worker.id() = transport.id()
}

/**
 * Tracks only expands to provisioned devices
 */
class WorkerTracker(val transport: Transport) {
    private val devices: ConcurrentHashMap<String, DeviceState> = ConcurrentHashMap()
    private val logger = MarathonLogging.logger { }

    private fun AppleTarget.id(): String {
        return when (this) {
            AppleTarget.Host -> "host"
            is AppleTarget.Physical -> udid
            is AppleTarget.Simulator -> udid
            is AppleTarget.SimulatorProfile -> "${fullyQualifiedDeviceTypeId}-${fullyQualifiedRuntimeId ?: ""}"
        }
    }

    fun update(updatedDevices: List<AppleTarget>): List<Pair<AppleTarget, TrackingUpdate>> {
        val updates = mutableListOf<Pair<AppleTarget, TrackingUpdate>>()
        val (simulatorProfiles, stableDevices) = updatedDevices.partition { it is AppleTarget.SimulatorProfile }
        val simulatorProfilesDesiredCount = (simulatorProfiles as List<AppleTarget.SimulatorProfile>).groupBy { it.id() }
        val updateIds = simulatorProfilesDesiredCount.keys + stableDevices.map { it.id() }

        val devicesNotReportedAnymore = devices.filter { !updateIds.contains(it.key) }
        devicesNotReportedAnymore.forEach { (id, state) ->
            when (state) {
                is DeviceState.ONLINE -> {
                    updates.add(Pair(state.target, TrackingUpdate.Disconnected(state.target)))
                    devices.remove(id)
                }

                is DeviceState.PROVISIONED -> {
                    val currentCount = state.count
                    logger.warn { "Deprovisioning simulators is not supported. Current = $currentCount, desired = 0" }
                }

                is DeviceState.OFFLINE -> Unit
            }
        }

        //Devices that don't require provisioning
        stableDevices.forEach {
            val previousState = devices[it.id()]

            if (previousState == null) {
                devices[it.id()] = DeviceState.ONLINE(it)
                updates.add(Pair(it, TrackingUpdate.Connected(it)))
            } else {
                when {
                    previousState !is DeviceState.ONLINE -> {
                        devices[it.id()] = DeviceState.ONLINE(it)
                        updates.add(Pair(it, TrackingUpdate.Connected(it)))
                    }
                    else -> Unit
                }
            }
        }

        //Devices that require provisioning can repeat. These don't have a stable identifier like UDID
        simulatorProfilesDesiredCount.forEach { (id, desired) ->
            val previousState = devices[id]

            val target = desired.first()
            if (previousState == null) {
                devices[id] = DeviceState.PROVISIONED(target, desired.size.toUInt())
                updates.add(Pair(target, TrackingUpdate.Connected(target)))
            } else {
                val desiredCount = desired.size.toUInt()
                val currentCount = (previousState as DeviceState.PROVISIONED).count
                when {
                    desiredCount > currentCount -> {
                        devices[id] = DeviceState.PROVISIONED(target, desired.size.toUInt())
                        repeat((desiredCount - currentCount).toInt()) {
                            updates.add(Pair(target, TrackingUpdate.Connected(target)))
                        }
                    }
                    desiredCount == currentCount -> Unit
                    desiredCount < currentCount -> {
                        logger.warn { "Deprovisioning simulators is not supported. Current = $currentCount, desired = $desiredCount" }
                    }
                }
            }
        }

        return updates
    }
}

sealed class TrackingUpdate {
    data class Connected(val target: AppleTarget) : TrackingUpdate()
    data class Disconnected(val target: AppleTarget) : TrackingUpdate()
}

sealed class DeviceState(open val target: AppleTarget) {
    data class ONLINE(override val target: AppleTarget) : DeviceState(target)
    data class PROVISIONED(override val target: AppleTarget.SimulatorProfile, val count: UInt) : DeviceState(target)
    data class OFFLINE(override val target: AppleTarget) : DeviceState(target)
}

data class Device(
    val transport: Transport,
    val target: AppleTarget,
)
