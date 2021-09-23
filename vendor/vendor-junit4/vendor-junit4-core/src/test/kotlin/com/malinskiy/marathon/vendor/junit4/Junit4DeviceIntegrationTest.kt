package com.malinskiy.marathon.vendor.junit4

import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.SystemTimer
import com.malinskiy.marathon.vendor.junit4.parsing.RemoteJupiterTestParser
import com.malinskiy.marathon.vendor.junit4.rule.IntegrationTestRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.Clock

class Junit4DeviceIntegrationTest {
    @get:Rule
    val temp = TemporaryFolder()

    @get:Rule
    val integrationTestRule = IntegrationTestRule(temp)

    private lateinit var testBundleIdentifier: Junit4TestBundleIdentifier
    private lateinit var device: Junit4Device

    @Before
    fun setup() {
        testBundleIdentifier = Junit4TestBundleIdentifier()
        device = Junit4Device(integrationTestRule.configuration, SystemTimer(Clock.systemUTC()), testBundleIdentifier)

        runBlocking {
            device.prepare(integrationTestRule.configuration)
        }
    }

    @After
    fun teardown() {
        device.dispose()
    }

    @Test
    fun testSimpleTest() {
        runBlocking {
            val devicePoolId = DevicePoolId("test")

            val testParser = RemoteJupiterTestParser(testBundleIdentifier)
            val testList = testParser.extract(integrationTestRule.configuration)
            val tests = testList.filter { it.pkg == "com.malinskiy.marathon.vendor.junit4.integrationtests" && it.clazz == "SimpleTest" }
            val testBatch = TestBatch(tests)
            val deferred = CompletableDeferred<TestBatchResults>()
            val progressReporter = ProgressReporter(integrationTestRule.configuration).apply {
                testCountExpectation(devicePoolId, 5)
            }

            device.execute(integrationTestRule.configuration, devicePoolId, testBatch, deferred, progressReporter)

            val results = deferred.await()
            results.finished.size shouldBeEqualTo 3
            results.failed.size shouldBeEqualTo 2
            results.finished.any { it.test.method == "testSucceeds" && it.status == TestStatus.PASSED } shouldBeEqualTo true
            results.finished.any { it.test.method == "testAssumptionFails" && it.status == TestStatus.ASSUMPTION_FAILURE } shouldBeEqualTo true
            results.finished.any { it.test.method == "testIgnored" && it.status == TestStatus.IGNORED } shouldBeEqualTo true

            results.failed.any { it.test.method == "testFails" && it.status == TestStatus.FAILURE } shouldBeEqualTo true
            results.failed.any { it.test.method == "testFailsWithNoMessage" && it.status == TestStatus.FAILURE } shouldBeEqualTo true
        }
    }

    @Test
    fun testClassIgnoredTest() {
        runBlocking {
            val devicePoolId = DevicePoolId("test")

            val testParser = RemoteJupiterTestParser(testBundleIdentifier)
            val testList = testParser.extract(integrationTestRule.configuration)
            val tests = testList.filter { it.pkg == "com.malinskiy.marathon.vendor.junit4.integrationtests" && it.clazz == "IgnoredTest" }
            val testBatch = TestBatch(tests)
            val deferred = CompletableDeferred<TestBatchResults>()
            val progressReporter = ProgressReporter(integrationTestRule.configuration).apply {
                testCountExpectation(devicePoolId, 1)
            }

            device.execute(integrationTestRule.configuration, devicePoolId, testBatch, deferred, progressReporter)

            val results = deferred.await()
            results.finished.size shouldBeEqualTo 1
            results.failed.size shouldBeEqualTo 0
            val test = results.finished.first().test
            test.method shouldBeEqualTo "testIgnoredTest"
            test.clazz shouldBeEqualTo "IgnoredTest"
            test.pkg shouldBeEqualTo "com.malinskiy.marathon.vendor.junit4.integrationtests"
        }
    }

    @Test
    fun testParameterizedTest() {
        runBlocking {
            val devicePoolId = DevicePoolId("test")

            val testParser = RemoteJupiterTestParser(testBundleIdentifier)
            val testList = testParser.extract(integrationTestRule.configuration)
            val tests =
                testList.filter { it.pkg == "com.malinskiy.marathon.vendor.junit4.integrationtests" && it.clazz == "ParameterizedTest" }
            val testBatch = TestBatch(tests)
            val deferred = CompletableDeferred<TestBatchResults>()
            val progressReporter = ProgressReporter(integrationTestRule.configuration).apply {
                testCountExpectation(devicePoolId, 2)
            }

            device.execute(integrationTestRule.configuration, devicePoolId, testBatch, deferred, progressReporter)

            val results = deferred.await()
            results.finished.size shouldBeEqualTo 2
            results.failed.size shouldBeEqualTo 0

            results.finished.any { it.test.method == "testShouldCapitalize[a -> A]" && it.status == TestStatus.PASSED } shouldBeEqualTo true
            results.finished.any { it.test.method == "testShouldCapitalize[b -> B]" && it.status == TestStatus.PASSED } shouldBeEqualTo true
        }
    }

    @Test
    fun testCustomParameterizedTest() {
        runBlocking {
            val devicePoolId = DevicePoolId("test")

            val testParser = RemoteJupiterTestParser(testBundleIdentifier)
            val testList = testParser.extract(integrationTestRule.configuration)
            val tests =
                testList.filter { it.pkg == "com.malinskiy.marathon.vendor.junit4.integrationtests" && it.clazz == "CustomParameterizedTest" }
            val testBatch = TestBatch(tests)
            val deferred = CompletableDeferred<TestBatchResults>()
            val progressReporter = ProgressReporter(integrationTestRule.configuration).apply {
                testCountExpectation(devicePoolId, 2)
            }

            device.execute(integrationTestRule.configuration, devicePoolId, testBatch, deferred, progressReporter)

            val results = deferred.await()
            results.finished.size shouldBeEqualTo 2
            results.failed.size shouldBeEqualTo 0

            results.finished.any { it.test.method == "testcase1 raw" && it.status == TestStatus.PASSED } shouldBeEqualTo true
            results.finished.any { it.test.method == "testcase2 raw" && it.status == TestStatus.PASSED } shouldBeEqualTo true
        }
    }

    @Test
    fun testClassHierarchyExecution() {
        runBlocking {
            val devicePoolId = DevicePoolId("test")

            val testParser = RemoteJupiterTestParser(testBundleIdentifier)
            val testList = testParser.extract(integrationTestRule.configuration)
            val tests = testList.filter { it.pkg == "com.malinskiy.marathon.vendor.junit4.integrationtests" && it.clazz == "ChildTest" }
            val testBatch = TestBatch(tests)
            val deferred = CompletableDeferred<TestBatchResults>()
            val progressReporter = ProgressReporter(integrationTestRule.configuration).apply {
                testCountExpectation(devicePoolId, 2)
            }

            device.execute(integrationTestRule.configuration, devicePoolId, testBatch, deferred, progressReporter)

            val results = deferred.await()
            results.finished.size shouldBeEqualTo 2
            results.failed.size shouldBeEqualTo 0

            results.finished.any { it.test.method == "testChildPassed" && it.status == TestStatus.PASSED } shouldBeEqualTo true
            results.finished.any { it.test.method == "testPasses" && it.status == TestStatus.PASSED } shouldBeEqualTo true
        }
    }

    @Test
    fun testClassHierarchyWithAbstractParentExecution() {
        runBlocking {
            val devicePoolId = DevicePoolId("test")

            val testParser = RemoteJupiterTestParser(testBundleIdentifier)
            val testList = testParser.extract(integrationTestRule.configuration)
            val tests =
                testList.filter { it.pkg == "com.malinskiy.marathon.vendor.junit4.integrationtests" && it.clazz == "ChildFromAbstractTest" }
            val testBatch = TestBatch(tests)
            val deferred = CompletableDeferred<TestBatchResults>()
            val progressReporter = ProgressReporter(integrationTestRule.configuration).apply {
                testCountExpectation(devicePoolId, 2)
            }

            device.execute(integrationTestRule.configuration, devicePoolId, testBatch, deferred, progressReporter)

            val results = deferred.await()
            results.finished.size shouldBeEqualTo 2
            results.failed.size shouldBeEqualTo 0

            results.finished.any { it.test.method == "testParentDidSetup" && it.status == TestStatus.PASSED } shouldBeEqualTo true
            results.finished.any { it.test.method == "testParent" && it.status == TestStatus.PASSED } shouldBeEqualTo true
        }
    }

    @Test
    fun testEmptyBatch() {
        runBlocking {
            val devicePoolId = DevicePoolId("test")

            val testBatch = TestBatch(emptyList())
            val deferred = CompletableDeferred<TestBatchResults>()
            val progressReporter = ProgressReporter(integrationTestRule.configuration).apply {
                testCountExpectation(devicePoolId, 1)
            }

            device.execute(integrationTestRule.configuration, devicePoolId, testBatch, deferred, progressReporter)

            val results = deferred.await()
            results.finished.size shouldBeEqualTo 0
            results.failed.size shouldBeEqualTo 0
        }
    }
}
