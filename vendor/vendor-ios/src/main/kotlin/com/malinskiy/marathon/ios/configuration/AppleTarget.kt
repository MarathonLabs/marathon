package com.malinskiy.marathon.ios.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = AppleTarget.Simulator::class, name = "simulator"),
    JsonSubTypes.Type(value = AppleTarget.Physical::class, name = "physical"),
    JsonSubTypes.Type(value = AppleTarget.SimulatorProfile::class, name = "simulatorProfile"),
)
sealed class AppleTarget {
    data class Simulator(@JsonProperty("udid") val udid: String) : AppleTarget()

    /**
     * @param deviceTypeId A valid available device type.
     *      Find these by running "xcrun simctl list devicetypes".
     *      Examples: ("iPhone X", "com.apple.CoreSimulator.SimDeviceType.iPhone-X")
     *
     * @param runtimeId A valid and available runtime.
     *      Find these by running "xcrun simctl list runtimes".
     *      If no runtime is specified the newest runtime compatible with the device type is chosen.
     *      Examples: ("watchOS3", "watchOS3.2", "watchOS 3.2", "com.apple.CoreSimulator.SimRuntime.watchOS-3-2",
     *      "/Volumes/path/to/Runtimes/watchOS 3.2.simruntime")
     *
     * @param namePrefix If there is a need to create a new simulator the name of new simulator will contain this prefix
     */
    data class SimulatorProfile(
        @JsonProperty("deviceType") private val deviceTypeId: String,
        @JsonProperty("runtime") private val runtimeId: String? = null,
        @JsonProperty("newNamePrefix") val newNamePrefix: String = "marathon",
    ) : AppleTarget() {
        val fullyQualifiedRuntimeId: String? by lazy {
            runtimeId?.let { expandTextualId(it, FQCN_SIM_RUNTIME) }
        }
        val fullyQualifiedDeviceTypeId: String by lazy {
            expandTextualId(deviceTypeId, FQCN_SIM_DEVICE_TYPE)
        }

        private fun expandTextualId(id: String, expectedFQCN: String) = if (!id.startsWith(expectedFQCN)) {
            val safeId = StringBuilder().apply {
                id.forEach {
                    append(
                        when (it) {
                            ' ' -> '-'
                            '.' -> '-'
                            else -> it
                        }
                    )
                }
            }.toString()
            "$expectedFQCN.$safeId"
        } else {
            id
        }
    }

    data class Physical(@JsonProperty("udid") val udid: String) : AppleTarget()

    companion object {
        const val FQCN_SIM_DEVICE_TYPE = "com.apple.CoreSimulator.SimDeviceType"
        const val FQCN_SIM_RUNTIME = "com.apple.CoreSimulator.SimRuntime"
    }
}
