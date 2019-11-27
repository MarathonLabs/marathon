package com.malinskiy.marathon.cache.test

import com.malinskiy.marathon.cache.MemoryCacheService
import com.malinskiy.marathon.cache.SimpleCacheKey
import com.malinskiy.marathon.createDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestComponentInfo
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xdescribe
import java.io.File

class TestResultsCacheSpek : Spek(
    {
        val cache by memoized {
            val service = MemoryCacheService()
            TestResultsCache(service)
        }

        // ignored as it's not implemented yet
        xdescribe("TestResultsCache") {
            it("should return null when load test with empty cache") {
                runBlocking {
                    val result = cache.load(SimpleCacheKey("test"), createTest())

                    result shouldEqual null
                }
            }

            it("should return saved test result when load after saving") {
                runBlocking {
                    val test = Test(
                        pkg = "com.test",
                        clazz = "Test",
                        method = "test1",
                        componentInfo = TestComponentInfo(someInfo = "someInfo", name = "component-name"),
                        metaProperties = emptyList()
                    )
                    val deviceInfo = createDeviceInfo()
                    val testResult = TestResult(
                        test = test,
                        device = deviceInfo,
                        status = TestStatus.PASSED,
                        startTime = 123,
                        endTime = 456,
                        stacktrace = "stacktrace"
                    )

                    cache.store(SimpleCacheKey("test"), testResult)
                    val testResultFromCache = cache.load(SimpleCacheKey("test"), test)

                    testResultFromCache shouldNotEqual null
                    testResultFromCache!!.test shouldEqual test
                    testResultFromCache.device shouldEqual deviceInfo
                    testResultFromCache.status shouldEqual TestStatus.PASSED
                    testResultFromCache.startTime shouldEqual 123
                    testResultFromCache.endTime shouldEqual 456
                    testResultFromCache.stacktrace shouldEqual "stacktrace"
                }
            }

            it("should return saved test result when load after saving with attachment") {
                val tempFile = File.createTempFile("test", "123").apply {
                    writeText("abc")
                    deleteOnExit()
                }

                runBlocking {
                    val test = createTest()
                    val testResult = createTestResult(attachments = listOf(Attachment(tempFile, AttachmentType.LOG)))

                    cache.store(SimpleCacheKey("test"), testResult)
                    val result = cache.load(SimpleCacheKey("test"), test)

                    result shouldNotEqual null
                    result!!.attachments.size shouldEqual 1
                    result.attachments.first().file.readText() shouldEqual "abc"
                    result.attachments.first().type shouldEqual FileType.LOG
                }
            }
        }
    })

private fun createTestResult(attachments: List<Attachment>) = TestResult(
    test = createTest(),
    device = createDeviceInfo(),
    status = TestStatus.PASSED,
    startTime = 123,
    endTime = 456,
    attachments = attachments
)

private fun createTest() = Test(
    pkg = "com.test",
    clazz = "Test",
    method = "test1",
    componentInfo = TestComponentInfo(someInfo = "someInfo", name = "component-name"),
    metaProperties = emptyList()
)
