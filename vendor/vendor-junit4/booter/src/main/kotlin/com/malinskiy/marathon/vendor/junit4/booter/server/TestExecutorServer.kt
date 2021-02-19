package com.malinskiy.marathon.vendor.junit4.booter.server

import com.malinskiy.marathon.vendor.junit4.contract.EventType
import com.malinskiy.marathon.vendor.junit4.contract.TestEvent
import com.malinskiy.marathon.vendor.junit4.contract.TestExecutorGrpcKt
import com.malinskiy.marathon.vendor.junit4.contract.TestRequest
import io.grpc.Server
import io.grpc.ServerBuilder
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.junit.runner.Description
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import org.junit.runner.Result
import org.junit.runner.manipulation.Filter
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import kotlin.system.exitProcess

class TestExecutorServer(private val port: Int) {
    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(TestExecutorService())
        .build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@TestExecutorServer.stop()
                println("*** server shut down")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    private class TestExecutorService : TestExecutorGrpcKt.TestExecutorCoroutineImplBase() {
        override fun execute(request: TestRequest): Flow<TestEvent> {
            val tests = request.fqtnList

            val core = JUnitCore()

            val klasses = mutableSetOf<Class<*>>()
            val testDescriptions = tests.map { fqtn ->
                val klass = fqtn.substringBefore('#')
                val loadClass = Class.forName(klass)
                klasses.add(loadClass)

                val method = fqtn.substringAfter('#')
                Description.createTestDescription(loadClass, method)
            }.toHashSet()

            val request = Request.classes(*klasses.toTypedArray()).filterWith(TestFilter(testDescriptions))

            return callbackFlow {
                val callback = object : RunListener() {
                    override fun testRunStarted(description: Description) {
                        super.testRunStarted(description)
                        try {
                            sendBlocking(
                                TestEvent.newBuilder()
                                    .setEventType(EventType.RUN_STARTED)
                                    .build()
                            )
                        } catch (e: Exception) {
                            // Handle exception from the channel: failure in flow or premature closing
                        }
                    }

                    override fun testRunFinished(result: Result) {
                        super.testRunFinished(result)
                        try {
                            sendBlocking(
                                TestEvent.newBuilder()
                                    .setEventType(EventType.RUN_FINISHED)
                                    .setTotalDurationMillis(result.runTime)
                                    .build()
                            )
                        } catch (e: Exception) {
                            // Handle exception from the channel: failure in flow or premature closing
                        }

                        channel.close()
                    }

                    override fun testStarted(description: Description) {
                        super.testStarted(description)
                        try {
                            sendBlocking(
                                TestEvent.newBuilder()
                                    .setEventType(EventType.TEST_STARTED)
                                    .setClassname(description.className)
                                    .setMethod(description.methodName)
                                    .setTestCount(description.testCount())
                                    .build()
                            )
                        } catch (e: Exception) {
                            // Handle exception from the channel: failure in flow or premature closing
                        }
                    }

                    override fun testFinished(description: Description) {
                        super.testFinished(description)
                        try {
                            sendBlocking(
                                TestEvent.newBuilder()
                                    .setEventType(EventType.TEST_FINISHED)
                                    .setClassname(description.className)
                                    .setMethod(description.methodName)
                                    .build()
                            )
                        } catch (e: Exception) {
                            // Handle exception from the channel: failure in flow or premature closing
                        }
                    }

                    override fun testFailure(failure: Failure) {
                        super.testFailure(failure)
                        try {
                            sendBlocking(
                                TestEvent.newBuilder()
                                    .setEventType(EventType.TEST_FAILURE)
                                    .setClassname(failure.description.className)
                                    .setMethod(failure.description.methodName)
                                    .setMessage(failure.message)
                                    .setStacktrace(failure.trace)
                                    .build()
                            )
                        } catch (e: Exception) {
                            // Handle exception from the channel: failure in flow or premature closing
                        }
                    }

                    override fun testAssumptionFailure(failure: Failure) {
                        super.testAssumptionFailure(failure)
                        try {
                            sendBlocking(
                                TestEvent.newBuilder()
                                    .setEventType(EventType.TEST_ASSUMPTION_FAILURE)
                                    .setClassname(failure.description.className)
                                    .setMethod(failure.description.methodName)
                                    .setMessage(failure.message)
                                    .setStacktrace(failure.trace)
                                    .build()
                            )
                        } catch (e: Exception) {
                            // Handle exception from the channel: failure in flow or premature closing
                        }
                    }

                    override fun testIgnored(description: Description) {
                        super.testIgnored(description)
                        try {
                            sendBlocking(
                                TestEvent.newBuilder()
                                    .setEventType(EventType.TEST_IGNORED)
                                    .setClassname(description.className)
                                    .setMethod(description.methodName)
                                    .build()
                            )
                        } catch (e: Exception) {
                            // Handle exception from the channel: failure in flow or premature closing
                        }
                    }
                }
                core.addListener(callback)
                val result = core.run(request)
                println(
                    """
                    Success: ${result.wasSuccessful()}
                    Tests: ${result.runCount}
                    Ignored: ${result.ignoreCount}
                    Failures: ${result.failureCount}
                    ${result.failures.joinToString("\n") { "${it.description.displayName}: ${it.message}" }}
                    """.trimIndent()
                )
                awaitClose {
                    core.removeListener(callback)
                    exitProcess(0)
                }
            }
        }
    }
}

class TestFilter(private val testDescriptions: HashSet<Description>) : Filter() {
    override fun shouldRun(description: Description): Boolean {
        if (description.isTest) {
            return testDescriptions.contains(description)
        }

        // explicitly check if any children want to run
        for (each in description.children) {
            if (shouldRun(each!!)) {
                return true
            }
        }
        return false
    }

    override fun describe() = "Marathon JUnit4 execution filter"
}
