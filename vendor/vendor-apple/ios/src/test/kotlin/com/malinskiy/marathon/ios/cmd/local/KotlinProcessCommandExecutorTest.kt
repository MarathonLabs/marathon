package com.malinskiy.marathon.ios.cmd.local

import com.malinskiy.marathon.apple.ios.cmd.BaseCommandExecutorTest

class KotlinProcessCommandExecutorTest : BaseCommandExecutorTest() {
    override fun createExecutor() = KotlinProcessCommandExecutor()
}
