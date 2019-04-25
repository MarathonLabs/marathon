package com.malinskiy.marathon

interface OutputPrinter {
    fun print(testCount: Int)
}

class StandardOutputPrinter: OutputPrinter {
    override fun print(testCount: Int) {
        System.out.println(testCount)
    }
}