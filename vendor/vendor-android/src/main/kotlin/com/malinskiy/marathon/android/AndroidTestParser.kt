package com.malinskiy.marathon.android

import com.linkedin.dex.parser.DecodedValue
import com.linkedin.dex.parser.DexParser
import com.linkedin.dex.parser.TestAnnotation
import com.malinskiy.marathon.execution.ComponentInfo
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.Test

class AndroidTestParser : TestParser {
    override fun extract(componentInfo: ComponentInfo): List<Test> {
        val androidComponentInfo = componentInfo as AndroidComponentInfo

        val tests = DexParser.findTestMethods(androidComponentInfo.testApplicationOutput.absolutePath)
        return tests.map {
            val testName = it.testName
            val annotations = it.annotations.map { it.toMetaProperty() }
            val split = testName.split("#")

            if (split.size != 2) throw IllegalStateException("Can't parse test $testName")

            val methodName = split[1]
            val packageAndClassName = split[0]

            val lastDotIndex = packageAndClassName.indexOfLast { c -> c == '.' }
            val packageName = packageAndClassName.substring(0 until lastDotIndex)
            val className = packageAndClassName.substring(lastDotIndex + 1 until packageAndClassName.length)

            Test(packageName, className, methodName, annotations, componentInfo)
        }.also {
            if (it.isEmpty()) {
                throw NoTestCasesFoundException("No tests cases were found in the test APK: ${androidComponentInfo.testApplicationOutput.absolutePath}")
            }
        }
    }
}

private fun TestAnnotation.toMetaProperty(): MetaProperty {
    val metaMap = values.mapValues {
        val value = it.value
        val realValue = when (value) {
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
        }
        realValue
    }
    return MetaProperty(name, metaMap)
}