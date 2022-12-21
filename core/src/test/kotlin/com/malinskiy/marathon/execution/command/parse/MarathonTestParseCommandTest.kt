package com.malinskiy.marathon.execution.command.parse

import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val RESULT_FILE_NAME = "result"
private const val RESULT_FILE_NAME_WITH_EXT = RESULT_FILE_NAME + EXTENSION

class MarathonTestParseCommandTest {

    @field:TempDir
    lateinit var tempRootDir: File

    private lateinit var marathonTestParseCommand: MarathonTestParseCommand

    @BeforeEach
    fun beforeEachTest() {
        marathonTestParseCommand = MarathonTestParseCommand(tempRootDir)
    }

    @Test
    fun `test parse command should create a file with zero tests`() {
        val testList = listOf<com.malinskiy.marathon.test.Test>()

        marathonTestParseCommand.execute(testList, RESULT_FILE_NAME)

        val file = File(tempRootDir.absolutePath, RESULT_FILE_NAME_WITH_EXT)
        file.exists() shouldBe true

        val content = file.readText()
        content.trimIndent() shouldBeEqualTo zeroTestsResult
    }

    @Test
    fun `test parse command should create a file with one test`() {
        val test1 = com.malinskiy.marathon.test.Test("com.example", "SimpleTest", "method1", emptyList())
        val testList = listOf(test1)

        marathonTestParseCommand.execute(testList, RESULT_FILE_NAME)

        val file = File(tempRootDir.absolutePath, RESULT_FILE_NAME_WITH_EXT)
        file.exists() shouldBe true

        val content = file.readText()
        content.trimIndent() shouldBeEqualTo oneTestResult
    }

    @Test
    fun `test parse command should create a file with several tests`() {
        val test1 = com.malinskiy.marathon.test.Test("com.example", "SimpleTest", "method1", emptyList())
        val test2 = com.malinskiy.marathon.test.Test("com.example", "SimpleTest", "method2", emptyList())
        val test3 = com.malinskiy.marathon.test.Test("com.example", "SimpleTest", "method3", emptyList())
        val testList = listOf(test1, test2, test3)

        marathonTestParseCommand.execute(testList, RESULT_FILE_NAME)

        val file = File(tempRootDir.absolutePath, RESULT_FILE_NAME_WITH_EXT)
        file.exists() shouldBe true

        val content = file.readText()
        content.trimIndent() shouldBeEqualTo severalTestsResult
    }
}

private val zeroTestsResult: String = """---
tests: []""".trimIndent()

private val oneTestResult: String = """---
tests:
- pkg: "com.example"
  clazz: "SimpleTest"
  method: "method1"
  metaProperties: []""".trimIndent()

private val severalTestsResult: String = """---
tests:
- pkg: "com.example"
  clazz: "SimpleTest"
  method: "method1"
  metaProperties: []
- pkg: "com.example"
  clazz: "SimpleTest"
  method: "method2"
  metaProperties: []
- pkg: "com.example"
  clazz: "SimpleTest"
  method: "method3"
  metaProperties: []""".trimIndent()
