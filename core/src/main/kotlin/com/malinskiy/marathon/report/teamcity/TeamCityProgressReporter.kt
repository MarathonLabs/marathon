package com.malinskiy.marathon.report.teamcity

import com.malinskiy.marathon.report.ProgressReporter

class TeamCityProgressReporter : ProgressReporter {
    private fun setBuildMessage(message: String) = println("##teamcity[buildStatus text='$message']")

    private fun setKeyValue(key: String, value: String) = println("##teamcity[buildStatisticValue key='$key' value='$value']")

    fun setBuildProgress(int: Int) = setBuildMessage("Marathon run: $int %")

    override fun testStarted(progress: Float, poolId: String, deviceSerial: String, testName: String) {
        setBuildProgress(progress.toInt())
    }
}
