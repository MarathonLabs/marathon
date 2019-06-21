package com.malinskiy.marathon

interface OutputPrinter {
    fun print(testCount: Int)
    fun print(versionString: String)
}

class StandardOutputPrinter: OutputPrinter {
    override fun print(testCount: Int) {
        System.out.println(testCount)
    }

    override fun print(versionString: String) {
        System.out.println(versionString)
    }
}