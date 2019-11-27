package com.malinskiy.marathon.cache.test

import com.malinskiy.marathon.cache.CacheKey
import com.malinskiy.marathon.cache.CacheService
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.test.Test

class TestResultsCache(private val cacheService: CacheService) {

    suspend fun load(cacheKey: CacheKey, test: Test): TestResult? {
        return null
    }

    suspend fun store(cacheKey: CacheKey, testResult: TestResult) {

    }

    fun terminate() {
        cacheService.close()
    }
}
