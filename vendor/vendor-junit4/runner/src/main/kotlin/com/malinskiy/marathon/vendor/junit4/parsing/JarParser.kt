package com.malinskiy.marathon.vendor.junit4.parsing

import com.malinskiy.marathon.test.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class JarParser {
    fun findTests(file: File): List<Test> {
        val result = mutableListOf<Test>()
        visitClasses(file) { classZipEntry, inputStream ->
            val classReader = ClassReader(inputStream)
            val visitor = JarClassVisitor(Opcodes.ASM6)
            classReader.accept(visitor, 0)

            val fqcn = classZipEntry.fqcn
            val pkg = fqcn.substringBeforeLast('.')
            val className = fqcn.substringAfterLast('.')

            result.addAll(
                visitor.methods
                    .filter { it.annotations.contains("Lorg/junit/Test;") }
                    .map { method ->
                        Test(
                            pkg = pkg,
                            clazz = className,
                            method = method.name,
                            metaProperties = emptySet()
                        )
                    }
            )
        }
        return result.toList()
    }

    private fun <T> visitClasses(file: File, classVisitor: (ZipEntry, ZipInputStream) -> T) {
        if (!file.exists()) throw FileNotFoundException("File $file doesn't exit")
        if (!file.isFile) throw RuntimeException("File $file is not a file")
        if (file.extension != "jar") throw RuntimeException("File $file is not a jar")

        val stream = ZipInputStream(BufferedInputStream(FileInputStream(file)))
        val entries = generateSequence {
            stream.nextEntry
        }

        for (entry in entries) {
            when {
                entry.isDirectory -> continue
                entry.name.endsWith(".class") -> {
                    classVisitor(entry, stream)
                }
            }
        }
    }
}

private val ZipEntry.fqcn
    get() = name.replace('/', '.').removeSuffix(".class")
