package com.malinskiy.marathon.report.steps


import com.malinskiy.marathon.test.Test


interface StepsJsonProvider {
    fun registerListener(listener: StepsJsonListener)
}

interface StepsJsonListener {
    fun onStepsJsonAttached(test: Test, stepsJson: String)
}