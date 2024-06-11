package com.malinskiy.marathon.apple.plist

import com.dd.plist.BinaryPropertyListWriter
import com.dd.plist.NSObject
import com.dd.plist.PropertyListParser
import com.dd.plist.XMLPropertyListWriter
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.full.primaryConstructor

abstract class PropertyList<T: NSObject>(val delegate: T) {
    @Suppress("UNCHECKED_CAST")
    companion object {
        inline fun <S, reified T : PropertyList<S>> from(file: File): T = from(PropertyListParser.parse(file))
        inline fun <S, reified T : PropertyList<S>> from(inputStream: InputStream): T = from(PropertyListParser.parse(inputStream))
        inline fun <S, reified T : PropertyList<S>> from(filePath: String): T = from(PropertyListParser.parse(filePath))
        inline fun <S, reified T : PropertyList<S>> from(bytes: ByteArray): T = from(PropertyListParser.parse(bytes))
        inline fun <S, reified T : PropertyList<S>> from(nsObject: NSObject): T {
            return T::class.primaryConstructor?.call(nsObject)
                ?: throw IllegalArgumentException("type ${T::class.qualifiedName} should define primary constructor with a single parameter of type NSObject")
        }
    }

    fun saveAsBinary(file: File) {
        BinaryPropertyListWriter.write(delegate, file)
    }

    fun saveAsBinary(outputStream: OutputStream) {
        BinaryPropertyListWriter.write(delegate, outputStream)
    }

    fun saveAsXML(file: File) {
        XMLPropertyListWriter.write(delegate, file)
    }

    fun saveAsXML(outputStream: OutputStream) {
        XMLPropertyListWriter.write(delegate, outputStream)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PropertyList<*>

        if (delegate != other.delegate) return false

        return true
    }

    override fun hashCode(): Int {
        return delegate.hashCode()
    }
}
