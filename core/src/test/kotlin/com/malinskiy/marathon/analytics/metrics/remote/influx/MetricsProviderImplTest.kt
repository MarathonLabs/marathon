package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.analytics.external.MetricsProviderImpl
import com.malinskiy.marathon.analytics.metrics.remote.ExecutionTime
import com.malinskiy.marathon.analytics.metrics.remote.RemoteDataSource
import com.malinskiy.marathon.analytics.metrics.remote.SuccessRate
import com.malinskiy.marathon.generateTest
import com.malinskiy.marathon.test.toSafeTestName
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.mock
import org.amshove.kluent.shouldEqualTo
import org.junit.jupiter.api.Test
import java.time.Instant

class MetricsProviderImplTest {
    @Test
    fun testExecutionTimeFromCache() {
        val dataStore = mock<RemoteDataSource>()
        val provider = MetricsProviderImpl(dataStore)
        val test = generateTest()
        val requestPercentile = 90.0
        val resultTime = 100.5
        val limit = Instant.now()
        val list =
            listOf(ExecutionTime(test.toSafeTestName(), resultTime), ExecutionTime("test", 80.0))
        whenever(
            dataStore.requestAllExecutionTimes(
                eq(requestPercentile),
                eq(limit)
            )
        ).thenReturn(list)
        provider.executionTime(test, requestPercentile, limit) shouldEqualTo resultTime
        verify(dataStore).requestAllExecutionTimes(eq(requestPercentile), eq(limit))
        verifyNoMoreInteractions(dataStore)
        provider.executionTime(test, requestPercentile, limit) shouldEqualTo resultTime
    }

    @Test
    fun testExecutionTimeFromMissingCache() {
        val dataStore = mock<RemoteDataSource>()
        val provider = MetricsProviderImpl(dataStore)
        val test = generateTest()
        val firstPercent = 80.0
        val firstTime = 900.0
        val firstLimit = Instant.now().minusSeconds(60)
        val firstList =
            listOf(ExecutionTime(test.toSafeTestName(), firstTime), ExecutionTime("test", 80.0))
        whenever(
            dataStore.requestAllExecutionTimes(
                eq(firstPercent),
                eq(firstLimit)
            )
        ).thenReturn(firstList)
        provider.executionTime(test, firstPercent, firstLimit) shouldEqualTo firstTime
        verify(dataStore).requestAllExecutionTimes(eq(firstPercent), eq(firstLimit))

        val secondLimit = Instant.now()
        val secondPercent = 90.0
        val secondTime = 1500.0
        val secondList =
            listOf(ExecutionTime(test.toSafeTestName(), secondTime), ExecutionTime("test", 90.0))
        whenever(
            dataStore.requestAllExecutionTimes(
                eq(secondPercent),
                eq(secondLimit)
            )
        ).thenReturn(secondList)
        provider.executionTime(test, secondPercent, secondLimit) shouldEqualTo secondTime
        verify(dataStore).requestAllExecutionTimes(eq(secondPercent), eq(secondLimit))
    }

    @Test
    fun testSuccessRateFromCache() {
        val dataStore = mock<RemoteDataSource>()
        val provider = MetricsProviderImpl(dataStore)
        val test = generateTest()
        val mean = 90.0
        val limit = Instant.now()
        val list = listOf(SuccessRate(test.toSafeTestName(), mean), SuccessRate("test", 80.0))
        whenever(dataStore.requestAllSuccessRates(limit)).thenReturn(list)
        provider.successRate(test, limit) shouldEqualTo mean
        verify(dataStore).requestAllSuccessRates(eq(limit))
        verifyNoMoreInteractions(dataStore)
        provider.successRate(test, limit) shouldEqualTo mean
    }

    @Test
    fun testSuccessRateWithMissingCache() {
        val dataStore = mock<RemoteDataSource>()
        val provider = MetricsProviderImpl(dataStore)
        val test = generateTest()
        val firstMean = 90.0
        val firstLimit = Instant.now().minusSeconds(60)
        val firstList =
            listOf(SuccessRate(test.toSafeTestName(), firstMean), SuccessRate("test", 80.0))
        whenever(dataStore.requestAllSuccessRates(firstLimit)).thenReturn(firstList)
        provider.successRate(test, firstLimit) shouldEqualTo firstMean
        verify(dataStore).requestAllSuccessRates(eq(firstLimit))

        val secondLimit = Instant.now()
        val secondMean = 80.0
        val secondList =
            listOf(SuccessRate(test.toSafeTestName(), secondMean), SuccessRate("test", 90.0))
        whenever(dataStore.requestAllSuccessRates(secondLimit)).thenReturn(secondList)
        provider.successRate(test, secondLimit) shouldEqualTo secondMean
        verify(dataStore).requestAllSuccessRates(eq(secondLimit))
    }
}
