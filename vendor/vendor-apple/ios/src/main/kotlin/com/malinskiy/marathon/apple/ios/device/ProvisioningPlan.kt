package com.malinskiy.marathon.apple.ios.device

import com.malinskiy.marathon.apple.configuration.AppleTarget

data class ProvisioningPlan(
    val existingSimulators: Set<String>,
    val needsProvisioning: List<AppleTarget.SimulatorProfile>,
    val physical: Set<String>
)
