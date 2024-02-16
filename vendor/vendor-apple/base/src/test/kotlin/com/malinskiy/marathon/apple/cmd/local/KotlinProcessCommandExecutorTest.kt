package com.malinskiy.marathon.apple.cmd.local

import com.malinskiy.marathon.apple.cmd.BaseCommandExecutorTest


class KotlinProcessCommandExecutorTest : BaseCommandExecutorTest() {
    override fun createExecutor() = KotlinProcessCommandExecutor()
}
