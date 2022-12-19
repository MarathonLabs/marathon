//package com.malinskiy.marathon.ios.cmd.remote
//
//import com.malinskiy.marathon.ios.cmd.remote.ssh.legacy.SshjCommandOutputWaiterImpl
//import kotlinx.coroutines.runBlocking
//import org.amshove.kluent.shouldBe
//import org.amshove.kluent.shouldBeGreaterOrEqualTo
//import org.junit.jupiter.api.Test
//import kotlin.system.measureTimeMillis
//
//class SshjCommandOutputWaiterTest {
//    private val testOutputTimeoutMillis = 100L
//    private val sleepDurationMillis = 15L
//    private val waiter = SshjCommandOutputWaiterImpl(
//        testOutputTimeoutMillis,
//        sleepDurationMillis
//    )
//
//    @Test
//    fun `waiter updated within timeout should not be expired`() {
//        waiter.update()
//        Thread.sleep(testOutputTimeoutMillis / 2)
//
//        waiter.isExpired shouldBe false
//    }
//
//    @Test
//    fun `timeout is over since waiter was updated should be expired`() {
//        waiter.update()
//        Thread.sleep(testOutputTimeoutMillis * 2)
//
//        waiter.isExpired shouldBe true
//    }
//
//    @Test
//    fun `wait is called should block for configured duration`() {
//        runBlocking {
//            val elapsedTime = measureTimeMillis {
//                waiter.wait()
//            }
//
//            elapsedTime shouldBeGreaterOrEqualTo sleepDurationMillis
//        }
//    }
//}
