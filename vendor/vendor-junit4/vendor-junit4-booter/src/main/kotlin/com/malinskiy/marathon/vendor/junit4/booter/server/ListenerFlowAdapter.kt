package com.malinskiy.marathon.vendor.junit4.booter.server

import com.malinskiy.marathon.vendor.junit4.booter.contract.EventType
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestEvent
import com.malinskiy.marathon.vendor.junit4.booter.filter.TestFilter
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.sendBlocking
import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

class ListenerFlowAdapter(
    private val producer: ProducerScope<TestEvent>,
    private val actualClassLocator: MutableMap<Description, String>
) : RunListener() {

    override fun testRunStarted(description: Description) {
        super.testRunStarted(description)
        try {
            producer.sendBlocking(
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
            producer.sendBlocking(
                TestEvent.newBuilder()
                    .setEventType(EventType.RUN_FINISHED)
                    .setTotalDurationMillis(result.runTime)
                    .build()
            )
        } catch (e: Exception) {
            // Handle exception from the channel: failure in flow or premature closing
        }
        producer.close()
    }

    override fun testStarted(description: Description) {
        super.testStarted(description)
        val description = description.toActualDescription(actualClassLocator)
        try {
            producer.sendBlocking(
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
        val description = description.toActualDescription(actualClassLocator)
        try {
            producer.sendBlocking(
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
        val description = failure.description.toActualDescription(actualClassLocator)
//                        println(failure.exception.cause?.printStackTrace())
        try {
            producer.sendBlocking(
                TestEvent.newBuilder()
                    .setEventType(EventType.TEST_FAILURE)
                    .setClassname(description.className)
                    .setMethod(description.methodName)
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
        val description = failure.description.toActualDescription(actualClassLocator)
        try {
            producer.sendBlocking(
                TestEvent.newBuilder()
                    .setEventType(EventType.TEST_ASSUMPTION_FAILURE)
                    .setClassname(description.className)
                    .setMethod(description.methodName)
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
        val description = description.toActualDescription(actualClassLocator)
        try {
            producer.sendBlocking(
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

private fun Description.toActualDescription(actualClassLocator: MutableMap<Description, String>): Description {
    return if (className == TestFilter.CLASS_NAME_STUB) {
        Description.createTestDescription(actualClassLocator[this], methodName, *annotations.toTypedArray())
    } else {
        this
    }
}
