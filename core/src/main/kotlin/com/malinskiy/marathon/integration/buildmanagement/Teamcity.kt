package com.malinskiy.marathon.integration.buildmanagement

object Teamcity : BuildManagement {
    override fun setBuildMessage(message: String) = println("##teamcity[buildStatus text='$message']")
    override fun setKeyValue(key: String, value: String) = println("##teamcity[buildStatisticValue key='$key' value='$value']")
}
