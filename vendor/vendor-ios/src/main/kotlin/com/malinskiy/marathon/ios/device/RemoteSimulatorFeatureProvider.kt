package com.malinskiy.marathon.ios.device

import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.ios.IOSDevice
import com.malinskiy.marathon.ios.cmd.remote.SshjCommandException

object RemoteSimulatorFeatureProvider {
    fun deviceFeatures(device: IOSDevice): Collection<DeviceFeature> {
        return enumValues<DeviceFeature>().filter { featureIsAvailable(device, it) }
    }

    private fun featureIsAvailable(device: IOSDevice, feature: DeviceFeature): Boolean = when (feature) {
        DeviceFeature.SCREENSHOT -> true
        DeviceFeature.VIDEO -> {
            device.hostCommandExecutor.execBlocking(
                "/usr/sbin/system_profiler -detailLevel mini -xml SPDisplaysDataType"
            ).let {
                it.exitStatus == 0
                        && it.stdout.contains("spdisplays_metalfeatureset")
            }
        }
    }

    fun availablePort(device: IOSDevice): Int {
        val commandResult = device.hostCommandExecutor.execBlocking(
            """ruby -e 'require "socket"; puts Addrinfo.tcp("", 0).bind {|s| s.local_address.ip_port }'"""
        )
        return when {
            commandResult.exitStatus == 0 -> commandResult.stdout.trim().toIntOrNull()
            else -> null
        } ?: throw SshjCommandException(commandResult.stdout)
    }
}
