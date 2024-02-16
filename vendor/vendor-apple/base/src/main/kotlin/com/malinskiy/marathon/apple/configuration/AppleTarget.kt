package com.malinskiy.marathon.apple.configuration

@com.fasterxml.jackson.annotation.JsonTypeInfo(
    use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME,
    include = com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@com.fasterxml.jackson.annotation.JsonSubTypes(
    com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = AppleTarget.Simulator::class, name = "simulator"),
    com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = AppleTarget.Physical::class, name = "physical"),
    com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = AppleTarget.SimulatorProfile::class, name = "simulatorProfile"),
    com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = AppleTarget.Host::class, name = "host")
)
sealed class AppleTarget {
    data class Simulator(@com.fasterxml.jackson.annotation.JsonProperty("udid") val udid: String) : AppleTarget()

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
        @com.fasterxml.jackson.annotation.JsonProperty("deviceType") private val deviceTypeId: String,
        @com.fasterxml.jackson.annotation.JsonProperty("runtime") private val runtimeId: String? = null,
        @com.fasterxml.jackson.annotation.JsonProperty("newNamePrefix") val newNamePrefix: String = "marathon",
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

    data class Physical(@com.fasterxml.jackson.annotation.JsonProperty("udid") val udid: String) : AppleTarget()

    /**
     * Used for native macOS testing to indicate that marathon should use the host itself rather than something started on the host
     */
    data object Host : AppleTarget()

    companion object {
        const val FQCN_SIM_DEVICE_TYPE = "com.apple.CoreSimulator.SimDeviceType"
        const val FQCN_SIM_RUNTIME = "com.apple.CoreSimulator.SimRuntime"
    }
}
