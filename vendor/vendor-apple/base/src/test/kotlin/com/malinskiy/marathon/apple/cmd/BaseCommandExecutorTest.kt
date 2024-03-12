@file:OptIn(ExperimentalTime::class)

package com.malinskiy.marathon.apple.cmd

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInRange
import org.amshove.kluent.shouldHaveSize
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.jupiter.api.Test
import java.nio.charset.Charset
import java.time.Duration
import kotlin.time.ExperimentalTime

abstract class BaseCommandExecutorTest {
    abstract fun createExecutor(): CommandExecutor

    @Test
    fun testStdout() = runTest {
        createExecutor().use { executor ->
            val command = executor.execute(
                listOf("echo", "Hello world"),
                Duration.ofSeconds(5),
                Duration.ofSeconds(1),
                emptyMap(),
                null,
                Charset.defaultCharset()
            )
            val result = command.await()
            result.stdout shouldBeEqualTo listOf("Hello world")
            result.stderr shouldHaveSize 0
            result.exitCode shouldBeEqualTo 0
            command.isAlive
        }
    }

    @Test
    fun testStderr() = runTest {
        createExecutor().use { executor ->
            val command =
                executor.execute(
                    listOf("bash", "-c", "1>/dev/stderr echo Hello world"),
                    Duration.ofSeconds(5),
                    Duration.ofSeconds(1),
                    emptyMap(),
                    null,
                    Charset.defaultCharset()
                )
            val result = command.await()

            result.stdout shouldHaveSize 0
            result.stderr shouldBeEqualTo listOf("Hello world")
            result.exitCode shouldBeEqualTo 0
        }
    }


    @Test
    fun testExitCode() = runTest {
        createExecutor().use { executor ->
            val command =
                executor.execute(
                    listOf("sh", "-c", "exit 1"),
                    Duration.ofSeconds(5),
                    Duration.ofSeconds(1),
                    emptyMap(),
                    null,
                    Charset.defaultCharset()
                )
            val result = command.await()
            result.stdout shouldHaveSize 0
            result.stderr shouldHaveSize 0
            result.exitCode shouldBeEqualTo 1
        }
    }

    @Test
    fun testBuffering() = runTest {
        createExecutor().use { executor ->
            val command =
                executor.execute(
                    listOf("sh", "-c", "seq 1 100000"),
                    Duration.ofSeconds(10),
                    Duration.ofSeconds(1),
                    emptyMap(),
                    null,
                    Charset.defaultCharset()
                )
            val result = command.await()
            result.stdout shouldHaveSize 100000
            result.stderr shouldHaveSize 0
            result.exitCode shouldBeEqualTo 0
        }
    }

    @Test
    fun testTimeout() = runTest {
        createExecutor().use { executor ->
            val command =
                executor.execute(
                    listOf("sh", "-c", "sleep 100"),
                    Duration.ofMillis(10),
                    Duration.ofSeconds(1),
                    emptyMap(),
                    null,
                    Charset.defaultCharset()
                )
            val result = command.await()
            result.exitCode shouldBeEqualTo null
        }
    }

    @Test
    fun testTerminate() = runTest {
        createExecutor().use { executor ->
            val command =
                executor.execute(
                    listOf("sh", "-c", "while [ true ]; do date && sleep 0.1; done"),
                    Duration.ofSeconds(5),
                    Duration.ofSeconds(1),
                    emptyMap(),
                    null,
                    Charset.defaultCharset()
                )
            val resultDeferred = async {
                command.await()
            }

            //We really want to block for 500ms since actual shell invocation can't be advanced
            withContext(Dispatchers.IO) {
                delay(500)
            }
            command.terminate()

            val result = resultDeferred.await()
            result.stdout.size shouldBeInRange 3..10 //fuzzy way of checking that we didn't actually produce all 50 lines
            result.stderr shouldHaveSize 0
            result.exitCode shouldBeEqualTo 143 //graceful termination
        }
    }
    
    @Test
    fun testTerminateShouldNotCloseExecutor() {
        runBlocking {
            createExecutor().use { executor ->
                val sessionA = executor.execute(
                    listOf(
                        "sleep", "1000",
                    ), Duration.ofMinutes(10), Duration.ofMinutes(10), emptyMap(), null
                )
                val deferredA = async {
                    sessionA.await()
                }
                delay(1000)
                sessionA.terminate()
                val commandResult = deferredA.await()
                commandResult.exitCode shouldBeEqualTo 143

                val sessionB = executor.execute(listOf("echo", "hello"), Duration.ofSeconds(10), Duration.ofSeconds(10), emptyMap(), null)
                val resultB = sessionB.await()
                resultB.combinedStdout shouldBeEqualTo "hello"
            }
        }
    }
    
    @Test
    fun testInterruptUsingKillShouldNotCloseExecutor() {
        runBlocking {
            createExecutor().use { executor ->
                val sessionA = executor.execute(
                    listOf(
                        "sleep", "100000", "&"
                    ), Duration.ofMinutes(10), Duration.ofMinutes(10), emptyMap(), null
                )
                val deferredA = async {
                    sessionA.await()
                }
                delay(1000)
                
                val commandResult = deferredA.await()
                //exit code is not standardized across OSes: some use 143 to indicate SIGTERM, but not all
                commandResult.exitCode shouldNotBeEqualTo 0

                val sessionB = executor.execute(listOf("echo", "hello"), Duration.ofSeconds(10), Duration.ofSeconds(10), emptyMap(), null)
                val resultB = sessionB.await()
                resultB.combinedStdout shouldBeEqualTo "hello"
            }
        }
    }
}
