package com.malinskiy.marathon.report

class CompositeSummaryPrinter(private val list: List<SummaryPrinter>) : SummaryPrinter {
    override fun print(summary: Summary) {
        list.forEach {
            it.print(summary)
        }
    }
}
