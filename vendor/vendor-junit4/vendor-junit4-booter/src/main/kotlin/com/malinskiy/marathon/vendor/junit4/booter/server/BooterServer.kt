package com.malinskiy.marathon.vendor.junit4.booter.server

import com.malinskiy.marathon.vendor.junit4.booter.Mode
import com.malinskiy.marathon.vendor.junit4.booter.exec.ExecutionMode
import com.malinskiy.marathon.vendor.junit4.booter.exec.InProcessTestExecutor
import com.malinskiy.marathon.vendor.junit4.booter.exec.IsolatedTestExecutor
import io.grpc.Server
import io.grpc.ServerBuilder
import kotlin.system.exitProcess

class BooterServer(private val port: Int, private val mode: Mode, private val executionMode: ExecutionMode) {
    private val server: Server = ServerBuilder
        .forPort(port)
        .apply {
            when (mode) {
                Mode.RUNNER -> addService(
                    TestExecutorService(
                        when (executionMode) {
                            ExecutionMode.INPROCESS -> InProcessTestExecutor()
                            ExecutionMode.ISOLATED -> IsolatedTestExecutor()
                        }
                    )
                )
                Mode.DISCOVER -> addService(TestParserService())
            }
        }
        .maxInboundMessageSize((32 * 1e6).toInt())
        .build()

    fun start() {
        try {
            withRetry(10, 100) {
                server.start()
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            exitProcess(1)
        }

        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** JVM is shutting down: shutting down gRPC server")
                this@BooterServer.stop()
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

}

@Suppress("TooGenericExceptionCaught")
fun <T> withRetry(attempts: Int, delayTime: Long = 0, f: () -> T): T {
    var attempt = 1
    while (true) {
        try {
            return f()
        } catch (th: Throwable) {
            if (attempt == attempts) {
                throw th
            } else {
                Thread.sleep(delayTime)
            }
        }
        ++attempt
    }
}
