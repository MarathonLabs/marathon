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

    // testable target properties

    private val target = PropertyListKey.ModuleName(
            propertyList.keys.firstOrNull()
                    ?: throw IllegalArgumentException("xctestrun file does not define any testable targets")
    )

    /**
     * Indentifier
     */
    val targetName = this.target.toKeyString()

    /**
     * @see <a href="x-man-page://5/xcodebuild.xctestrun">xcodebuild.xctestrun(5)</a>
     */
    val isUITestBundle = propertyList.valueForKeypath(target, PropertyListKey.IsUITestBundle) as Boolean

    /**
     * @see <a href="x-man-page://5/xcodebuild.xctestrun">xcodebuild.xctestrun(5)</a>
     */
    val environmentVariables = propertyList.valueForKeypath(target, PropertyListKey.EnvironmentVariables) as PropertyListMap

    /**
     * @see <a href="x-man-page://5/xcodebuild.xctestrun">xcodebuild.xctestrun(5)</a>
     */
    val testingEnvironmentVariables = propertyList.valueForKeypath(target, PropertyListKey.TestingEnvironmentVariables) as PropertyListMap

    /**
     * @see <a href="x-man-page://5/xcodebuild.xctestrun">xcodebuild.xctestrun(5)</a>
     */
    private val skipTestIdentifiers = propertyList.valueForKeypath(target, PropertyListKey.SkipTestIdentifiers) as Array<Any>

    /**
     * Returns `true` if specified test should be excluded from the test run.
     */
    @suppress("ReturnCount")
    fun isSkipped(test: Test): Boolean {
        if (test.pkg != targetName) return false

        val skippedMethods = skippedTestMethodsByClass[test.clazz] ?: return false

        return skippedMethods.isEmpty() || skippedMethods.contains(test.method)
    }

    // property list manipulation

    /**
     * Defines a new environment variable or updates an existing one.
     */
    fun environment(name: String, value: String) {
        environmentVariables.put(name, value)
    }

    /**
     * Defines or updates environment variables with values from specified map.
     */
    fun environment(environmentVariables:  Map<String,String>) {
        this.environmentVariables.putAll(environmentVariables)
    }

    /**
     * Defines a new environment variable or updates an existing one.
     */
    fun testingEnvironment(name: String, value: String) {
        testingEnvironmentVariables[name] = value
    }

    /**
     * Defines or updates environment variables with values from specified map.
     */
    fun testingEnvironment(environmentVariables:  Map<String,String>) {
        testingEnvironmentVariables.putAll(environmentVariables)
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

    private val skippedTestMethodsByClass: Map<String, List<String>> by lazy {
        skipTestIdentifiers
                .map {
                    val parts = it.toString().split("/")
                    parts.first() to parts.getOrNull(1)
                }
                .groupBy(
                        { it.first },
                        { it.second }
                )
                .mapValues { it.value.filterNotNull() }
    }
}

private const val unchecked = "UNCHECKED_CAST"
