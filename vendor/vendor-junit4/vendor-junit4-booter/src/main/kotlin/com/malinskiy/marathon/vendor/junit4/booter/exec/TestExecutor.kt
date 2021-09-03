package com.malinskiy.marathon.vendor.junit4.booter.exec

import com.malinskiy.marathon.vendor.junit4.booter.contract.TestDescription
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestEvent
import kotlinx.coroutines.flow.Flow

interface TestExecutor {
    fun run(
        tests: MutableList<TestDescription>,
        javaHome: String?,
        javaOptions: List<String>,
        classpathList: MutableList<String>,
        workdir: String
    ): Flow<TestEvent>
}

