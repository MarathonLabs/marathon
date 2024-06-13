package com.malinskiy.marathon.report.stdout

import com.github.ajalt.mordant.rendering.BorderType.Companion.SQUARE_DOUBLE_SECTION_SEPARATOR
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.Companion.rgb
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.report.Reporter
import com.malinskiy.marathon.time.Timer
import org.apache.commons.lang3.time.DurationFormatUtils

class StdoutReporter(private val timer: Timer) : Reporter {
    override fun generate(executionReport: ExecutionReport) {
        val summary = executionReport.summary
        if (summary.pools.isEmpty()) return

        val terminal = Terminal()
        terminal.println(TextColors.cyan("Marathon run finished:"))
        summary.pools.forEach { poolSummary ->
            terminal.println(TextColors.cyan("Device pool ${poolSummary.poolId.name}:"))
            terminal.println(TextColors.cyan("\t${poolSummary.passed.size} passed, ${poolSummary.failed.size} failed, ${poolSummary.ignored.size} ignored tests"))
            terminal.println(TextColors.cyan("   Raw: ${poolSummary.rawPassed.size} passed, ${poolSummary.rawFailed.size} failed, ${poolSummary.rawIgnored.size} ignored, ${poolSummary.rawIncomplete.size} incomplete tests\n"))

            if (poolSummary.rawFailed.isNotEmpty()) {
                terminal.println(table {
                    borderType = SQUARE_DOUBLE_SECTION_SEPARATOR
                    borderStyle = rgb("#4b25b9")
                    align = TextAlign.RIGHT
                    tableBorders = Borders.NONE
                    header {
                        cellBorders = Borders.NONE
                        style = TextColors.red + TextStyles.bold
                        align = TextAlign.CENTER
                        row {
                            cell("Failed tests (raw): ${TextColors.brightRed(poolSummary.rawFailed.size.toString())}") {
                                columnSpan = 2
                            }
                        }
                        row ("Test name", "times") {}
                    }
                    body {
                        column(0) { align = TextAlign.LEFT }
                        column(1) {
                            align = TextAlign.RIGHT
                            style = TextColors.brightRed
                        }
                        poolSummary.rawFailed
                            .groupBy { it }
                            .toSortedMap()
                            .mapValues { it.value.size }
                            .forEach { (testName, count) -> row(testName, count) }
                    }
                    footer {
                        style(italic = true)
                        column(1) { style = TextColors.red }
                        row {
                            cells("Flakiness overhead: ", formatDuration(poolSummary.rawDurationMillis - poolSummary.durationMillis))
                        }
                    }
                })
            }
            terminal.println()

            if (poolSummary.rawIncomplete.isNotEmpty()) {
                terminal.println(table {
                    borderType = SQUARE_DOUBLE_SECTION_SEPARATOR
                    borderStyle = rgb("#4b25b9")
                    align = TextAlign.RIGHT
                    tableBorders = Borders.NONE
                    header {
                        cellBorders = Borders.NONE
                        style = TextColors.yellow + TextStyles.bold
                        align = TextAlign.CENTER
                        row {
                            cell("Incomplete tests (raw): ${TextColors.brightYellow(poolSummary.rawIncomplete.size.toString())}") {
                                columnSpan = 2
                            }
                        }
                        row ("Test name", "times") {}
                    }
                    body {
                        column(0) { align = TextAlign.LEFT }
                        column(1) {
                            align = TextAlign.RIGHT
                            style = TextColors.brightYellow
                        }
                        poolSummary.rawIncomplete
                            .groupBy { it }
                            .toSortedMap()
                            .mapValues { it.value.size }
                            .forEach { (testName, count) -> row(testName, count) }
                    }
                })
            }
            terminal.println(TextColors.cyan("\nTotal time: ${formatDuration(timer.elapsedTimeMillis)}\n"))
        }
    }

    private fun formatDuration(millis: Long) = if(millis > 0) DurationFormatUtils.formatDuration(millis, "H'H' mm'm' ss's'") else "0s"
}
