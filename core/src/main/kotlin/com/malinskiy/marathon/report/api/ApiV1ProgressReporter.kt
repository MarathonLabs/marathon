package com.malinskiy.marathon.report.api

import com.google.protobuf.Duration
import com.google.protobuf.Timestamp
import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.coroutines.newCoroutineExceptionHandler
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.ProgressReporter
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import io.grpc.ManagedChannelBuilder
import io.marathonlabs.testing.progress.DeviceBill
import io.marathonlabs.testing.progress.ProgressServiceGrpcKt
import io.marathonlabs.testing.progress.StartRequest
import io.marathonlabs.testing.progress.Stats
import io.marathonlabs.testing.progress.TestStat
import io.marathonlabs.testing.progress.TrackRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import java.time.Instant

class ApiV1ProgressReporter(private val target: String, private val runId: String) : ProgressReporter, CoroutineScope {
    private val logger = MarathonLogging.logger(ApiV1ProgressReporter::class.java.simpleName)
    private val dispatcher by lazy {
        newFixedThreadPoolContext(1, "ApiV1ProgressReporter")
    }

    override val coroutineContext = dispatcher + newCoroutineExceptionHandler(logger)

    private val client: ProgressServiceGrpcKt.ProgressServiceCoroutineStub by lazy {
        val channel = ManagedChannelBuilder.forTarget(target)
            .usePlaintext()
            .build()
        ProgressServiceGrpcKt.ProgressServiceCoroutineStub(channel)
    }

    private var pendingUpdate: TrackRequest? = null

    override fun begin(parsedFilteredTests: List<Test>) {
        val testPlan = StartRequest.newBuilder()
            .addAllTests(parsedFilteredTests.into())
            .setRunId(runId)
            .build()
        runBlocking {
            client.start(testPlan)
        }
    }

    override fun testStarted(progress: Float, poolId: String, deviceSerial: String, testName: String) {
        synchronized(this) {
            if (pendingUpdate != null) {
                TrackRequest.newBuilder(pendingUpdate)
                    .build()
            } else {
                null
            }
        }
    }

    override fun testFailed(progress: Float, poolId: String, deviceSerial: String, testName: String) {
        synchronized(this) {
            if (pendingUpdate != null) {
                TrackRequest.newBuilder(pendingUpdate)
                    .build()
            } else {
                null
            }
        }
    }

    override fun testPassed(progress: Float, poolId: String, deviceSerial: String, testName: String) {
        synchronized(this) {
            if (pendingUpdate != null) {
                TrackRequest.newBuilder(pendingUpdate)
                    .build()
            } else {
                null
            }
        }
    }

    override fun testIgnored(progress: Float, poolId: String, deviceSerial: String, testName: String) {
        synchronized(this) {
            if (pendingUpdate != null) {
                TrackRequest.newBuilder(pendingUpdate)
                    .build()
            } else {
                null
            }
        }
    }

    override fun testIncomplete(progress: Float, poolId: String, deviceSerial: String, testName: String) {
        synchronized(this) {
            if (pendingUpdate != null) {
                TrackRequest.newBuilder(pendingUpdate)
                    .build()
            } else {
                null
            }
        }
    }

    override fun end(executionReport: ExecutionReport) {
        runBlocking {
            client.end(executionReport.into())
        }
    }

    private fun List<Test>.into(): List<io.marathonlabs.testing.progress.Test> {
        return map { test ->
            io.marathonlabs.testing.progress.Test.newBuilder()
                .setPkg(test.pkg)
                .setClazz(test.clazz)
                .setMethod(test.method)
                .addAllMetaProperties(test.metaProperties.map { it.name })
                .build()
        }.toList()
    }

    private fun ExecutionReport.into(): io.marathonlabs.testing.progress.EndRequest {
        val cost = bills.fold(java.time.Duration.ZERO) { acc, bill -> acc + bill.duration }

        val passed = summary.pools.sumOf { it.passed.size }
        val failed = summary.pools.sumOf { it.failed.size }
        val ignored = summary.pools.sumOf { it.ignored.size }
        val finalStats = Stats.newBuilder()
            .setPassed(passed)
            .setFailed(failed)
            .setIgnored(ignored)
            .setIncomplete(0) //Final tests can't be incomplete - they terminate as failed
            .build()

        val rawPassed = summary.pools.sumOf { it.rawPassed.size }
        val rawFailed = summary.pools.sumOf { it.rawFailed.size }
        val rawIgnored = summary.pools.sumOf { it.rawIgnored.size }
        val rawIncomplete = summary.pools.sumOf { it.rawIncomplete.size }
        val rawStats = Stats.newBuilder()
            .setPassed(rawPassed)
            .setFailed(rawFailed)
            .setIgnored(rawIgnored)
            .setIncomplete(rawIncomplete)
            .build()

        val finalFailedTests = summary.pools
            .flatMap { it.failed }
            .map {
                TestStat.newBuilder()
                    .setTest(it.into())
                    .setCount(1)
                    .build()
            }
        val rawFailedTests = summary.pools
            .flatMap { it.rawFailed }
            .groupBy { it.toTestName() }
            .toSortedMap()
            .map {
                TestStat.newBuilder()
                    .setTest(it.value.first().into())
                    .setCount(it.value.size)
                    .build()
            }
        val rawIncompleteTests = summary.pools
            .flatMap { it.rawIncomplete }
            .groupBy { it.toTestName() }
            .toSortedMap()
            .map {
                TestStat.newBuilder()
                    .setTest(it.value.first().into())
                    .setCount(it.value.size)
                    .build()
            }

        return io.marathonlabs.testing.progress.EndRequest.newBuilder()
            .setRunId(runId)
            .setSuccess(result)
            .setFlakiness(convertDuration(flakiness))
            .setCost(convertDuration(cost))
            .setDuration(convertDuration(duration))
            .setRaw(rawStats)
            .setFinal(finalStats)
            .addAllFinalFailed(finalFailedTests)
            .addAllRawFailed(rawFailedTests)
            .addAllRawIncomplete(rawIncompleteTests)
            .addAllDeviceBill(bills.map { it.into() })
            .build()
    }

    private fun com.malinskiy.marathon.report.bill.DeviceBill.into(): DeviceBill {
        return DeviceBill.newBuilder()
            .setStart(convertTimestamp(start))
            .setEnd(convertTimestamp(end))
            .setDuration(convertDuration(duration))
            .build()
    }

    private fun convertDuration(duration: java.time.Duration): Duration = Duration.newBuilder()
        .setSeconds(duration.seconds)
        .setNanos(duration.nano)
        .build()

    private fun convertTimestamp(start: Instant): Timestamp = Timestamp.newBuilder()
        .setSeconds(start.epochSecond)
        .setNanos(start.nano)
        .build()

    private fun Test.into(): io.marathonlabs.testing.progress.Test? {
        return io.marathonlabs.testing.progress.Test.newBuilder()
            .setPkg(pkg)
            .setClazz(clazz)
            .setMethod(method)
            .addAllMetaProperties(metaProperties.map { it.name })
            .build()
    }
}
