package com.malinskiy.marathon.ios.xctestrun

import com.dd.plist.PropertyListParser
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.Suppress as suppress

sealed class TestBundleInfoKey(private val value: String): PropertyListKey {
    object CFBundleExecutable: TestBundleInfoKey("CFBundleExecutable")

    override fun toKeyString(): String = value
}

@suppress(unchecked)
class TestBundleInfo(inputStream: InputStream) {

    constructor(file: File) : this(FileInputStream(file))

    val propertyList: PropertyListMap = PropertyListParser
            .parse(inputStream)
            .toJavaObject() as? PropertyListMap
            ?: throw IllegalArgumentException("could not parse test runner bundle Info.plist")

    fun CFBundleExecutable(): String? = propertyList.valueForKeypath(TestBundleInfoKey.CFBundleExecutable) as String
}

private const val unchecked = "UNCHECKED_CAST"