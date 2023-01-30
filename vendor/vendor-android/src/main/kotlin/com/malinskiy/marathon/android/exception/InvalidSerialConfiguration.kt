package com.malinskiy.marathon.android.exception

import com.malinskiy.marathon.config.vendor.android.SerialStrategy

class InvalidSerialConfiguration(serialStrategy: SerialStrategy) :
    RuntimeException("Serial configuration was set to $serialStrategy, could not find expected serial")
