package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.analytics.metrics.remote.ExecutionTime
import com.malinskiy.marathon.analytics.metrics.remote.RemoteDataSource
import com.malinskiy.marathon.analytics.metrics.remote.SuccessRate
import com.malinskiy.marathon.generateTest
import com.malinskiy.marathon.test.toSafeTestName
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import org.amshove.kluent.mock
import org.amshove.kluent.shouldEqualTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.time.Instant

class InfluxMetricsProviderSpek : Spek({
    describe("") {
        group("Execution time") {
            it("Return from cache if present with the same params") {
                val dataStore = mock<RemoteDataSource>()
                val provider = InfluxMetricsProvider(dataStore)
                val test = generateTest()
                val requestPercentile = 90.0
                val resultTime = 100.5
                val limit = Instant.now()
                val list = listOf(ExecutionTime(test.toSafeTestName(), resultTime), ExecutionTime("test", 80.0))
                whenever(dataStore.requestAllExecutionTimes(eq(requestPercentile), eq(limit))).thenReturn(list)
                provider.executionTime(test, requestPercentile, limit) shouldEqualTo resultTime
                verify(dataStore).requestAllExecutionTimes(eq(requestPercentile), eq(limit))
                verifyNoMoreInteractions(dataStore)
                provider.executionTime(test, requestPercentile, limit) shouldEqualTo resultTime
            }
            it("Call again if missing in cache because of new params") {
                val dataStore = mock<RemoteDataSource>()
                val provider = InfluxMetricsProvider(dataStore)
                val test = generateTest()
                val firstPercent = 80.0
                val firstTime = 900.0
                val firstLimit = Instant.now().minusSeconds(60)
                val firstList = listOf(ExecutionTime(test.toSafeTestName(), firstTime), ExecutionTime("test", 80.0))
                whenever(dataStore.requestAllExecutionTimes(eq(firstPercent), eq(firstLimit))).thenReturn(firstList)
                provider.executionTime(test, firstPercent, firstLimit) shouldEqualTo firstTime
                verify(dataStore).requestAllExecutionTimes(eq(firstPercent), eq(firstLimit))

                val secondLimit = Instant.now()
                val secondPercent = 90.0
                val secondTime = 1500.0
                val secondList = listOf(ExecutionTime(test.toSafeTestName(), secondTime), ExecutionTime("test", 90.0))
                whenever(dataStore.requestAllExecutionTimes(eq(secondPercent), eq(secondLimit))).thenReturn(secondList)
                provider.executionTime(test, secondPercent, secondLimit) shouldEqualTo secondTime
                verify(dataStore).requestAllExecutionTimes(eq(secondPercent), eq(secondLimit))
            }
        }
        group("Success rate") {
            it("Return from cache if present with the same params") {
                val dataStore = mock<RemoteDataSource>()
                val provider = InfluxMetricsProvider(dataStore)
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
            it("Call again if missing in cache because of new params") {
                val dataStore = mock<RemoteDataSource>()
                val provider = InfluxMetricsProvider(dataStore)
                val test = generateTest()
                val firstMean = 90.0
                val firstLimit = Instant.now().minusSeconds(60)
                val firstList = listOf(SuccessRate(test.toSafeTestName(), firstMean), SuccessRate("test", 80.0))
                whenever(dataStore.requestAllSuccessRates(firstLimit)).thenReturn(firstList)
                provider.successRate(test, firstLimit) shouldEqualTo firstMean
                verify(dataStore).requestAllSuccessRates(eq(firstLimit))

                val secondLimit = Instant.now()
                val secondMean = 80.0
                val secondList = listOf(SuccessRate(test.toSafeTestName(), secondMean), SuccessRate("test", 90.0))
                whenever(dataStore.requestAllSuccessRates(secondLimit)).thenReturn(secondList)
                provider.successRate(test, secondLimit) shouldEqualTo secondMean
                verify(dataStore).requestAllSuccessRates(eq(secondLimit))
            }
        }
    }
})