package com.malinskiy.marathon.android

import com.linkedin.dex.parser.DecodedValue
import com.linkedin.dex.parser.DexParser
import com.linkedin.dex.parser.TestAnnotation
import com.malinskiy.marathon.android.extension.testBundlesCompat
import com.malinskiy.marathon.android.model.AndroidTestBundle
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.execution.LocalTestParser
import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.Test

class DexTestParser(
    private val vendorConfiguration: VendorConfiguration.AndroidConfiguration,
    private val testBundleIdentifier: AndroidTestBundleIdentifier
) : LocalTestParser {

    override suspend fun extract(): List<Test> {
        val testBundles = vendorConfiguration.testBundlesCompat()
        return testBundles.flatMap { bundle ->
            val tests = DexParser.findTestMethods(bundle.testApplication.absolutePath)
            val androidTestBundle =
                AndroidTestBundle(bundle.application, bundle.testApplication, bundle.extraApplications, bundle.splitApks)
            return@flatMap tests.map {
                val testName = it.testName
                val annotations = it.annotations.map { annotation -> annotation.toMetaProperty() }
                val split = testName.split("#")

                if (split.size != 2) throw IllegalStateException("Can't parse test $testName")

                val methodName = split[1]
                val packageAndClassName = split[0]

                val lastDotIndex = packageAndClassName.indexOfLast { c -> c == '.' }
                val packageName = packageAndClassName.substring(0 until lastDotIndex)
                val className = packageAndClassName.substring(lastDotIndex + 1 until packageAndClassName.length)

                val test = Test(packageName, className, methodName, annotations)
                testBundleIdentifier.put(test, androidTestBundle)
                test
            }
        }
    }
}

private fun TestAnnotation.toMetaProperty(): MetaProperty {
    val metaMap = values.mapValues {
        val realValue = when (val value = it.value) {
            is DecodedValue.DecodedString -> value.value
            is DecodedValue.DecodedByte -> value.value
            is DecodedValue.DecodedShort -> value.value
            is DecodedValue.DecodedChar -> value.value
            is DecodedValue.DecodedInt -> value.value
            is DecodedValue.DecodedLong -> value.value
            is DecodedValue.DecodedFloat -> value.value
            is DecodedValue.DecodedDouble -> value.value
            is DecodedValue.DecodedType -> value.value
            DecodedValue.DecodedNull -> null
            is DecodedValue.DecodedBoolean -> value.value
            is DecodedValue.DecodedEnum -> value.value
            is DecodedValue.DecodedArrayValue -> value.values
        }
        realValue
    }
    return MetaProperty(name, metaMap)
}
