package com.malinskiy.marathon.cache.test

import com.malinskiy.marathon.cache.CacheKey
import com.malinskiy.marathon.cache.CacheService
import com.malinskiy.marathon.cache.test.serialization.TestResultEntryReader
import com.malinskiy.marathon.cache.test.serialization.TestResultEntryWriter
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.io.AttachmentManager
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import java.lang.Exception

class TestResultsCache(
    private val cacheService: CacheService,
    private val attachmentManager: AttachmentManager
) {

    private val logger = MarathonLogging.logger("TestResultsCache")

    suspend fun load(key: CacheKey, test: Test): TestResult? {
        try {
            val reader = TestResultEntryReader(test, attachmentManager)
            if (!cacheService.load(key, reader)) {
                return null
            }
            return reader.testResult
        } catch (exception: Throwable) {
            logger.warn("Error during loading cache entry for $test", exception)
            return null
        }
    }

    suspend fun store(key: CacheKey, testResult: TestResult) {
        try {
            val writer = TestResultEntryWriter(testResult)
            cacheService.store(key, writer)
        } catch (exception: Throwable) {
            logger.warn("Error during storing cache entry for ${testResult.test}", exception)
        }
    }

    fun terminate() {
        cacheService.close()
    }
}
