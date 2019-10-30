package com.malinskiy.marathon.ios.cmd.remote

import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeGreaterOrEqualTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import kotlin.system.measureTimeMillis

object SshjCommandOutputWaiterSpek : Spek(
    {
        val testOutputTimeoutMillis = 100L
        val sleepDurationMillis = 15L
        val waiter by memoized {
            SshjCommandOutputWaiterImpl(
                testOutputTimeoutMillis,
                sleepDurationMillis
            )
        }
        given("waiter updated within timeout") {
            it("should not be expired") {
                waiter.update()
                Thread.sleep(testOutputTimeoutMillis / 2)

                waiter.isExpired shouldBe false
            }
        }
        given("timeout is over since waiter was updated") {
            it("should be expired") {
                waiter.update()
                Thread.sleep(testOutputTimeoutMillis * 2)

                waiter.isExpired shouldBe true
            }
        }
        given("wait is called") {
            it("should block for configured duration") {
                runBlocking {
                    val elapsedTime = measureTimeMillis {
                        waiter.wait()
                    }

                    elapsedTime shouldBeGreaterOrEqualTo sleepDurationMillis
                }

            }
        }
    })
