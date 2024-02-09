package com.malinskiy.marathon.ios.cmd.local

import com.malinskiy.marathon.apple.ios.cmd.local.KotlinProcessCommandExecutor
import com.malinskiy.marathon.ios.cmd.BaseCommandExecutorTest


class KotlinProcessCommandExecutorTest : BaseCommandExecutorTest() {
    override fun createExecutor() = KotlinProcessCommandExecutor()
}
