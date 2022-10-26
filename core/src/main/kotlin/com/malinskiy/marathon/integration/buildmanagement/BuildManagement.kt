package com.malinskiy.marathon.integration.buildmanagement

interface BuildManagement {
    fun setBuildMessage(message: String)
    fun setKeyValue(key: String, value: String)
}
