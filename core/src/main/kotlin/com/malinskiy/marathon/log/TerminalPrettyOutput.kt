package com.malinskiy.marathon.log

import com.github.ajalt.mordant.animation.coroutines.animateInCoroutine
import com.github.ajalt.mordant.animation.progress.MultiProgressBarAnimation
import com.github.ajalt.mordant.animation.progress.ProgressTask
import com.github.ajalt.mordant.animation.progress.addTask
import com.github.ajalt.mordant.animation.progress.advance
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Spinner
import com.github.ajalt.mordant.widgets.progress.completed
import com.github.ajalt.mordant.widgets.progress.percentage
import com.github.ajalt.mordant.widgets.progress.progressBar
import com.github.ajalt.mordant.widgets.progress.progressBarContextLayout
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import com.github.ajalt.mordant.widgets.progress.spinner
import com.github.ajalt.mordant.widgets.progress.text
import com.github.ajalt.mordant.widgets.progress.timeElapsed
import com.malinskiy.marathon.device.DevicePoolId
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
object TerminalPrettyOutput {
    private val logger = MarathonLogging.logger("TerminalPrettyOutput")

    val terminal = Terminal()
    private val animation = MultiProgressBarAnimation(terminal).animateInCoroutine()
    private val overallLayout = progressBarLayout(alignColumns = false) {
        text { (terminal.theme.success + TextStyles.bold)("Overall") }
        completed(style = terminal.theme.success)
        spinner(Spinner.Dots())
        timeElapsed(style = terminal.theme.warning, compact = false)
    }
    private val overall = animation.addTask(overallLayout, total = 0)

    private val poolLayout = progressBarContextLayout<String> {
        text(fps = animationFps, align = TextAlign.LEFT) { "Pool: $context" }
        progressBar(width = 40)
        percentage()
        completed(style = terminal.theme.success)
        spinner(Spinner.Dots())
        timeElapsed(style = terminal.theme.info)
    }
    private val progressBars = ConcurrentHashMap<DevicePoolId, ProgressTask<String>>()

    suspend fun launchAnimation() {
        animation.execute()
    }

    fun addProgressBar(poolId: DevicePoolId) {
        progressBars.computeIfAbsent(poolId) { id ->
            animation.addTask(poolLayout, id.name)}
        overall.update { total = total?.plus(1) }
    }

    fun updateProgressBar(poolId: DevicePoolId, t: Long) {
        val pb = progressBars[poolId]?.update { total = t }
        if (pb == null) logger.debug { "Progress bar ${poolId.name} not registered in animation" }
    }

    fun advanceProgressBar(poolId: DevicePoolId) {
        val pb = progressBars[poolId]
        if (pb != null) {
            pb.advance()
            if (pb.total == pb.completed) {
                overall.advance()
            }
        } else {
            logger.debug { "Progress bar ${poolId.name} not registered in animation" }
        }
    }
}
