package com.malinskiy.marathon.vendor.junit4.booter.server

import com.malinskiy.marathon.vendor.junit4.booter.contract.TestEvent
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestExecutorGrpcKt
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestRequest
import com.malinskiy.marathon.vendor.junit4.booter.exec.TestExecutor
import kotlinx.coroutines.flow.Flow

class TestExecutorService(private val executor: TestExecutor) : TestExecutorGrpcKt.TestExecutorCoroutineImplBase() {
    override fun execute(request: TestRequest): Flow<TestEvent> {
        try {
            val tests = request.testDescriptionList
            val classpathList = request.testEnvironment.classpathList
            val javaHome = request.testEnvironment.javaHome
            val javaOptionsList = request.testEnvironment.javaOptionsList
            return executor.run(tests, javaHome, javaOptionsList, classpathList)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
