package com.malinskiy.marathon.ios

import com.dd.plist.PropertyListParser
import com.dd.plist.NSDictionary
import com.dd.plist.NSString
import com.dd.plist.NSNumber
import com.dd.plist.NSArray
import com.malinskiy.marathon.test.Test
import mu.KotlinLogging
import java.io.File

private const val MODULE_NAME_KEY = "ProductModuleName"
private const val IS_UI_TEST_BUNDLE_KEY = "IsUITestBundle"
private const val SKIP_TEST_IDENTIFIERS_KEY = "SkipTestIdentifiers"

class XCTestRun(path: File) {

    private val logger = KotlinLogging.logger("XCTestRun")

    private val skippedTestMethodsByClass: Map<String, List<String>>
    val moduleName: String
    val isUITestBundle: Boolean

    init {
        val plist = PropertyListParser.parse(path) as NSDictionary

        val testTargetName = plist.keys.firstOrNull()
                ?: throw IllegalArgumentException("xctestrun file does not contain any runnable targets")

        val testTargetConfiguration = plist.objectForKey(testTargetName) as NSDictionary
        moduleName = (testTargetConfiguration.objectForKey(MODULE_NAME_KEY) as NSString).toString()
        isUITestBundle = (testTargetConfiguration.objectForKey(IS_UI_TEST_BUNDLE_KEY) as NSNumber).boolValue()

        skippedTestMethodsByClass = (testTargetConfiguration.objectForKey(SKIP_TEST_IDENTIFIERS_KEY) as NSArray)
                .array
                .map {
                    val parts = it.toString().split("/")
                    parts.first() to parts.getOrNull(1) }
                .groupBy(
                        { it.first },
                        { it.second }
                )
                .mapValues { it.value.filterNotNull() }
    }

    @Suppress("ReturnCount")
    fun isSkipped(test: Test): Boolean {
        if (test.pkg != moduleName) return false

        val skippedMethods = skippedTestMethodsByClass[test.clazz] ?: return false

        return skippedMethods.isEmpty() || skippedMethods.contains(test.method)
    }
}
