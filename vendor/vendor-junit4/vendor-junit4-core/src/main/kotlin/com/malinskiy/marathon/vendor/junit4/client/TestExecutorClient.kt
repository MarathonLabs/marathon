package com.malinskiy.marathon.vendor.junit4.client

import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import com.malinskiy.marathon.vendor.junit4.booter.contract.EventType
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestDescription
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestEnvironment
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestEvent
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestExecutorGrpcKt
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestRequest
import com.malinskiy.marathon.vendor.junit4.executor.listener.JUnit4TestRunListener
import com.malinskiy.marathon.vendor.junit4.model.TestIdentifier
import io.grpc.ManagedChannel
import io.grpc.StatusException
import kotlinx.coroutines.flow.collect
import java.io.Closeable
import java.io.File
import java.util.concurrent.TimeUnit

class TestExecutorClient(
    private val channel: ManagedChannel
) : Closeable {
    private val stub: TestExecutorGrpcKt.TestExecutorCoroutineStub =
        TestExecutorGrpcKt.TestExecutorCoroutineStub(channel)
            .withWaitForReady()
            .withMaxInboundMessageSize((32 * 1e6).toInt())
            .withMaxOutboundMessageSize((32 * 1e6).toInt())

    suspend fun execute(tests: List<Test>, applicationClasspath: List<File>, testClasspath: List<File>, listener: JUnit4TestRunListener) {
        val descriptions = tests.map {
            TestDescription.newBuilder()
                .apply {
                    fqtn = it.toTestName()
                }
                .build()
        }

        val testEnvironment = TestEnvironment.newBuilder()
            .addAllClasspath(testClasspath.map { it.absolutePath })
            .addAllClasspath(applicationClasspath.map { it.absolutePath })
            .build()

        val request = TestRequest.newBuilder()
            .addAllTestDescription(descriptions)
            .setTestEnvironment(testEnvironment)
            .build()

        val responseFlow = stub.execute(request)
        try {
            responseFlow.collect { event: TestEvent ->
                when (event.eventType) {
                    EventType.RUN_STARTED -> {
                        listener.testRunStarted("Marathon JUnit4 Test Run", event.testCount)
                    }
                    EventType.RUN_FINISHED -> {
                        listener.testRunEnded(event.totalDurationMillis, emptyMap())
                    }
                    EventType.TEST_STARTED -> {
                        listener.testStarted(TestIdentifier(event.classname, event.method))
                    }
                    EventType.TEST_FINISHED -> {
                        listener.testEnded(TestIdentifier(event.classname, event.method), emptyMap())
                    }
                    EventType.TEST_FAILURE -> {
                        listener.testFailed(TestIdentifier(event.classname, event.method), event.stacktrace)
                    }
                    EventType.TEST_ASSUMPTION_FAILURE -> {
                        listener.testAssumptionFailure(TestIdentifier(event.classname, event.method), event.stacktrace)
                    }
                    EventType.TEST_IGNORED -> {
                        listener.testIgnored(TestIdentifier(event.classname, event.method))
                    }
                    EventType.UNRECOGNIZED -> Unit
                }
            }
        } catch (e: StatusException) {
            e.printStackTrace()
            //gRPC connection closed
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("Flow collected")
    }

    override fun close() {
        channel.shutdown().awaitTermination(10, TimeUnit.SECONDS)
    }
}
