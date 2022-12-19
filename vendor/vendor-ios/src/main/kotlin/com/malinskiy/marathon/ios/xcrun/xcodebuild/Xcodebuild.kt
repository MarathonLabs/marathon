package com.malinskiy.marathon.ios.xcrun.xcodebuild

import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.CommandSession
import com.malinskiy.marathon.ios.test.TestRequest
import com.malinskiy.marathon.log.MarathonLogging
import java.time.Duration

class Xcodebuild(
    private val commandExecutor: CommandExecutor,
    private val configuration: Configuration
) {
    private val logger = MarathonLogging.logger {}

    suspend fun testWithoutBuilding(udid: String, request: TestRequest): CommandSession {
        val command = listOf(
            "xcrun", "xcodebuild", "test-without-building",
            "-xctestrun", request.xctestrun,
            *request.toXcodebuildTestFilter(),
            "-resultBundlePath", request.xcresult,
            "-destination", "\'platform=iOS simulator,id=$udid\'"
        )
        logger.debug { "Running ${command.joinToString(" ")}" }
        return commandExecutor.execute(
            command = command,
            timeout = Duration.ofMillis(configuration.testBatchTimeoutMillis),
            idleTimeout = Duration.ofMillis(configuration.testOutputTimeoutMillis),
            workdir = request.workdir,
            env = mapOf("NSUnbufferedIO" to "YES"),
        )
    }
}
