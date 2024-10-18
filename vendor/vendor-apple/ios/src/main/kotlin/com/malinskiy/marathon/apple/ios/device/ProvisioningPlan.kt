package com.malinskiy.marathon.apple.ios.device

import com.malinskiy.marathon.apple.configuration.AppleTarget
import com.malinskiy.marathon.apple.ios.model.UDID

/**
 * Also works as a de-provisioning plan
 */
data class ProvisioningPlan(
    val existingSimulators: Set<UDID>,
    val needsProvisioning: List<AppleTarget.SimulatorProfile>,
    val physical: Set<UDID>
)
