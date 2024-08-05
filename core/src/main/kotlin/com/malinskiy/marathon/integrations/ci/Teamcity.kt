package com.malinskiy.marathon.integrations.ci

object Teamcity: CI {
    private fun setBuildMessage(message: String) = println("##teamcity[buildStatus text='$message']")

    private fun setKeyValue(key: String, value: String) = println("##teamcity[buildStatisticValue key='$key' value='$value']")

    override fun setBuildProgress(int: Int) = setBuildMessage("Marathon run: $int %")
}
