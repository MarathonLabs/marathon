package com.malinskiy.marathon.ios.xctestrun

import com.dd.plist.PropertyListParser
import com.dd.plist.NSObject
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import kotlin.Suppress as suppress

@suppress(unchecked)
class Xctestrun(inputStream: InputStream) {

    constructor(file: File): this(FileInputStream(file))

    val logger = MarathonLogging.logger(javaClass.simpleName)

    val propertyList: PropertyListMap = PropertyListParser
            .parse(inputStream)
            .toJavaObject() as? PropertyListMap
            ?: throw IllegalArgumentException("could not parse xctestrun")

    private val targets = propertyList.keys
            .filter { it != XctestrunKey.__xctestrun_metadata__.toKeyString() }
            .map { XctestrunKey.TargetName(it).toEntry() }
            .toMap()
            .takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("xctestrun file does not define any testable targets")

    private fun <T> targetKeyValue(targetName: String, key: XctestrunKey): T? = targets[targetName]?.let {
        propertyList.valueForKeypath(it, key) as T
    }
//    // testable target properties
//
//    private val target = XctestrunKey.TargetName(
//            propertyList.keys.firstOrNull { it != XctestrunKey.__xctestrun_metadata__.toKeyString() }
//                    ?: throw IllegalArgumentException("xctestrun file does not define any testable targets")
//    )
//
    /**
     * Test target identifier. Used in test names specified with -onlyTesting: option passed to xcodebuild
     */
    val targetNames = targets.keys

    /**
     * Testable product module name. Appears in testing logs as a test identifier prefix.
     */
    fun productModuleName(targetName: String): String? = targetKeyValue(targetName, XctestrunKey.ProductModuleName)

    private fun testHostPath(targetName: String): String? = targetKeyValue(targetName, XctestrunKey.TestHostPath)

    private fun testHostBundle(targetName: String): String? = targetKeyValue(targetName, XctestrunKey.TestBundlePath)

    fun testHostBundlePath(targetName: String): String? {
        return testHostBundle(targetName)?.let { testHostBundle ->
            return testHostPath(targetName)?.let { testHostPath ->
                // __TESTHOST__/PlugIns/sample-appUITests.xctest
                // __TESTROOT__/Debug-iphonesimulator/sample-appUITests-Runner.app
                return testHostBundle.replace("""^__TESTHOST__""".toRegex(),
                        testHostPath.replace("""^__TESTROOT__/""".toRegex(), ""))
            }
        } ?: null
    }

    /**
     * @see <a href="x-man-page://5/xcodebuild.xctestrun">xcodebuild.xctestrun(5)</a>
     */
    fun  isUITestBundle(targetName: String): Boolean? = targetKeyValue(targetName, XctestrunKey.IsUITestBundle)

    /**
     * @see <a href="x-man-page://5/xcodebuild.xctestrun">xcodebuild.xctestrun(5)</a>
     */
    fun environmentVariables(targetName: String): PropertyListMap? = targetKeyValue(targetName, XctestrunKey.EnvironmentVariables)

    /**
     * @see <a href="x-man-page://5/xcodebuild.xctestrun">xcodebuild.xctestrun(5)</a>
     */
    fun testingEnvironmentVariables(targetName: String): PropertyListMap? = targetKeyValue(targetName, XctestrunKey.TestingEnvironmentVariables)

    /**
     * @see <a href="x-man-page://5/xcodebuild.xctestrun">xcodebuild.xctestrun(5)</a>
     */
    fun skipTestIdentifiers(targetName: String): Array<Any>? = targetKeyValue(targetName, XctestrunKey.SkipTestIdentifiers)

    /**
     * Returns `true` if specified test should be excluded from the test run.
     */
    @suppress("ReturnCount")
    fun isSkipped(test: Test): Boolean {
        val targetName = targetNameFromProductModuleName(test.pkg) ?: return false
        val skipped = skipTestIdentifiers(targetName) ?: return false

        return skipped.contains(test.clazz) || skipped.contains("${test.clazz}/${test.method}")
    }

    private fun targetNameFromProductModuleName(productModuleName: String): String? {
        return targetNames.first { productModuleName(it) == productModuleName }
    }

    // property list manipulation

    /**
     * Defines a new environment variable or updates an existing one in all target configurations.
     */
    fun allTargetsEnvironment(name: String, value: String) {
        targets.keys.forEach { targetName ->
            environmentVariables(targetName)?.put(name, value)
        }
    }

    /**
     * Defines a new environment variable or updates an existing one in the received target configuration.
     */
    fun environment(targetName: String, name: String, value: String) {
        environmentVariables(targetName)?.put(name, value)
    }

    /**
     * Defines or updates environment variables with values from specified map in the received target configuration.
     */
    fun environment(targetName: String, variables: Map<String,String>) {
        environmentVariables(targetName)?.putAll(variables)
    }

    /**
     * Defines a new environment variable or updates an existing one in the received target configuration.
     */
    fun testingEnvironment(targetName: String, name: String, value: String) {
        testingEnvironmentVariables(targetName)?.put(name, value)
    }

    /**
     * Defines or updates environment variables with values from specified map in the received target configuration.
     */
    fun testingEnvironment(targetName: String, variables: Map<String,String>) {
        testingEnvironmentVariables(targetName)?.putAll(variables)
    }

    // output

    /**
     * @return a String representation of this property list rendered in XML format
     */
    fun toXMLString(): String {
        return String(toXMLByteArray())
    }

    /**
     * @return a ByteArray representation of this property list rendered as an XML string.
     */
    fun toXMLByteArray(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        PropertyListParser.saveAsXML(
                NSObject.fromJavaObject(propertyList),
                outputStream)
        return outputStream.toByteArray()
    }

    /**
     * Creates a new instance of Xctestrun with the same data.
     *
     * @return a deep clone of this instance.
     */
    fun clone(): Xctestrun {
        return Xctestrun(inputStream = ByteArrayInputStream(toXMLByteArray()))
    }

    /**
     * Compares two Xctestrun instances
     */
    override operator fun equals(other: Any?): Boolean {
        if (other !is Xctestrun) {
            return false
        }
        return areEqual(other.propertyList, propertyList)
    }

    override fun hashCode(): Int {
        return propertyList.hashCode()
    }

    /**
     * Compares property list nodes generated with [PropertyListParser]. Unlike standard [equals] implementation,
     * ignores key order when comparing Map nodes, and element order when comparing Array nodes.
     */
    private fun areEqual(l: Any?, r: Any?): Boolean {
        if (l is Map<*, *> && r is Map<*,*>) {
            if (l.keys.size != r.keys.size) {
                return false
            }
            if (l.keys.toSet() != r.keys.toSet()) {
                return false
            }
            return l.keys.toSet().fold(true) { acc, key ->
                acc && areEqual(l[key], r[key])
            }
        } else if (l is Array<*> && r is Array<*>) {
            return (l.toSet() == r.toSet())
        }
        return l == r
    }
}

private const val unchecked = "UNCHECKED_CAST"
