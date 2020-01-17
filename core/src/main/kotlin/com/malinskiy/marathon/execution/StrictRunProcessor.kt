package com.malinskiy.marathon.execution

import com.malinskiy.marathon.test.Test

class StrictRunProcessor(private val configuration: StrictRunFilterConfiguration) {

    fun processShard(shard: TestShard): TestShard {
        var testsForStrictRun = emptyList<Test>()
        configuration.filter.forEach { testsForStrictRun = it.filter(shard.tests.toList()) }

        val strictRuns = arrayListOf<Test>()
        testsForStrictRun.forEach { test ->
            repeat(configuration.runs) {
                strictRuns.add(test)
            }
        }

        return TestShard(
            tests = shard.tests + strictRuns,
            flakyTests = shard.flakyTests
        )
    }
}
