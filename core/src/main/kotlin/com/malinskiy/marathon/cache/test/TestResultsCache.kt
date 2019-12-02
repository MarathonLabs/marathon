package com.malinskiy.marathon.cache.test

import com.malinskiy.marathon.cache.CacheKey
import com.malinskiy.marathon.cache.CacheService
import com.malinskiy.marathon.cache.test.serialization.TestResultEntryReader
import com.malinskiy.marathon.cache.test.serialization.TestResultEntryWriter
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.io.AttachmentManager
import com.malinskiy.marathon.test.Test

class TestResultsCache(
    private val cacheService: CacheService,
    private val attachmentManager: AttachmentManager
) {

    suspend fun load(key: CacheKey, test: Test): TestResult? {
        val reader = TestResultEntryReader(test, attachmentManager)
        if (!cacheService.load(key, reader)) {
            return null
        }
        return reader.testResult
    }

    suspend fun store(key: CacheKey, testResult: TestResult) {
        val writer = TestResultEntryWriter(testResult)
        cacheService.store(key, writer)
    }

    fun terminate() {
        cacheService.close()
    }
}
