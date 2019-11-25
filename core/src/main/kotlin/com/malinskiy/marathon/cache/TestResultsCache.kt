package com.malinskiy.marathon.cache

import com.malinskiy.marathon.execution.TestResult

class TestResultsCache(private val cacheService: CacheService) {

    suspend fun loadTestResult(cacheKey: CacheKey): TestResult? {
        return null
    }

    suspend fun storeTestResult(cacheKey: CacheKey, testResult: TestResult) {

    }

    fun terminate() {
        cacheService.close()
    }
}
