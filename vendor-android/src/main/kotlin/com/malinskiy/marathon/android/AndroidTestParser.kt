package com.malinskiy.marathon.android

import com.linkedin.dex.parser.DexParser
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.test.Test
import java.io.File

class AndroidTestParser : TestParser {
    override fun extract(file: File): List<Test> {
        val tests = DexParser.findTestMethods(file.absolutePath)
        return tests.map {
            val testName = it.testName
            val annotationNames = it.annotationNames
            val split = testName.split("#")

            if (split.size != 2) throw IllegalStateException("Can't parse test $testName")

            val methodName = split[1]
            val packageAndClassName = split[0]

            val lastDotIndex = packageAndClassName.indexOfLast { c -> c == '.' }
            val packageName = packageAndClassName.substring(0 until lastDotIndex)
            val className = packageAndClassName.substring(lastDotIndex + 1 until packageAndClassName.length)

            Test(packageName, className, methodName, annotationNames)
        }
    }
}