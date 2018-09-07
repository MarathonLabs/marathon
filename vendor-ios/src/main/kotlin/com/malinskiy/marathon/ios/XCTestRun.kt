package com.malinskiy.marathon.ios

import com.dd.plist.*
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.test.Test
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger { }

class XCTestRun(val path: File) {

    private data class SkippedTest(val className: String, val method: String? = null)

    private val skippedTestMethodsByClass: Map<String, List<String>>
    val moduleName: String
    val isUITestBundle: Boolean

    init {
        val plist = PropertyListParser.parse(path) as NSDictionary

        val testTargetName = plist.keys.firstOrNull()
        if (testTargetName ==  null) { throw IllegalArgumentException("xctestrun file does not contain any runnable targets") }

        val testTargetConfiguration = plist.objectForKey(testTargetName) as NSDictionary
        moduleName = (testTargetConfiguration.objectForKey("ProductModuleName") as NSString).toString()
        isUITestBundle = (testTargetConfiguration.objectForKey("IsUITestBundle") as NSNumber).boolValue()

        skippedTestMethodsByClass = (testTargetConfiguration.objectForKey("SkipTestIdentifiers") as NSArray)
                .array
                .map {
                    val parts = it.toString().split("/")
                    parts.first() to if (parts.count() == 2) { listOf(parts.last()) } else listOf<String>() }
                .groupBy({ it.first }, { it.second })
                .mapValues { it.value.flatten() }
    }

    fun isSkipped(test: Test): Boolean {
        if (test.pkg != moduleName) return false

        val skippedMethods = skippedTestMethodsByClass.get(test.clazz) ?: return false

        return skippedMethods.isEmpty() || skippedMethods.contains(test.method)
    }
}