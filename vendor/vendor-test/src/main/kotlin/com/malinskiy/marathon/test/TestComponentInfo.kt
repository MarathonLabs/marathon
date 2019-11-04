package com.malinskiy.marathon.test

import com.malinskiy.marathon.execution.ComponentInfo

data class TestComponentInfo(
    val someInfo: String = "test",
    override val name: String = "some-name"
) : ComponentInfo
