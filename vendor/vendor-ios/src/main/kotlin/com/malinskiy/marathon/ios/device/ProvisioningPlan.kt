package com.malinskiy.marathon.ios.device

import com.malinskiy.marathon.ios.configuration.AppleTarget

data class ProvisioningPlan(val existingSimulators: Set<String>, val needsProvisioning: List<AppleTarget.SimulatorProfile>, val physical: Set<String>)
