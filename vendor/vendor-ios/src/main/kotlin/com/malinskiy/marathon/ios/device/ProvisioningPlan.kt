package com.malinskiy.marathon.ios.device

import com.malinskiy.marathon.ios.configuration.AppleTarget

data class ProvisioningPlan(val existingSimulators: Set<String>, val needsProvisioning: Set<AppleTarget.SimulatorProfile>, val physical: Set<String>)
