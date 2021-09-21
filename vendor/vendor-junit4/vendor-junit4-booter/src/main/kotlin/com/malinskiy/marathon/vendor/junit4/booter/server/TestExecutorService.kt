package com.malinskiy.marathon.vendor.junit4.booter.server

import com.google.protobuf.Empty
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestEvent
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestExecutorGrpcKt
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestRequest
import com.malinskiy.marathon.vendor.junit4.booter.exec.TestExecutor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlin.system.exitProcess

class TestExecutorService(private val executor: TestExecutor) : TestExecutorGrpcKt.TestExecutorCoroutineImplBase() {
    override fun execute(request: TestRequest): Flow<TestEvent> {
        val tests = request.testDescriptionList
        val classpathList = request.testEnvironment.classpathList
        val javaHome = request.testEnvironment.javaHome
        val javaOptionsList = request.testEnvironment.javaOptionsList
        val workdir = request.testEnvironment.workdir
        return executor.run(tests, javaHome, javaOptionsList, classpathList, workdir)
            .catch { it.printStackTrace() }
    }

    override suspend fun terminate(request: Empty): Empty {
        executor.terminate()
        exitProcess(0)
        return super.terminate(request)
    }
}
