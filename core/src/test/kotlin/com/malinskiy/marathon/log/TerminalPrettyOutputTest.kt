package com.malinskiy.marathon.log

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.github.ajalt.mordant.animation.progress.ProgressTask
import com.malinskiy.marathon.device.DevicePoolId
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap

class TerminalPrettyOutputTest {

    @Test
    @Suppress("UNCHECKED_CAST")
    fun updateProgressBar() {
        val pool = DevicePoolId("PoolId")
        TerminalPrettyOutput.addProgressBar(pool)
        TerminalPrettyOutput.updateProgressBar(pool, 1)
        val f = TerminalPrettyOutput.javaClass.getDeclaredField("progressBars")
        if (f.trySetAccessible()) {
            val pbs = f.get(TerminalPrettyOutput) as ConcurrentHashMap<DevicePoolId, ProgressTask<String>>
            assertThat(pbs[pool]?.total).isEqualTo(1)
        }
    }
}
