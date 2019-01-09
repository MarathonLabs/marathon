package com.malinskiy.marathon.log

import com.malinskiy.marathon.vendor.VendorConfiguration

interface MarathonLogConfigurator {
    fun configure(vendorConfiguration: VendorConfiguration)
}
