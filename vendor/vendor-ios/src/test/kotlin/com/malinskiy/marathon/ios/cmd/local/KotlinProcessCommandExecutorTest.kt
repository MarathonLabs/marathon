package com.malinskiy.marathon.ios.cmd.local

import com.malinskiy.marathon.ios.cmd.BaseCommandExecutorTest
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test
import java.nio.charset.Charset
import java.time.Duration

class KotlinProcessCommandExecutorTest : BaseCommandExecutorTest() {
    override fun createExecutor() = KotlinProcessCommandExecutor()
}
