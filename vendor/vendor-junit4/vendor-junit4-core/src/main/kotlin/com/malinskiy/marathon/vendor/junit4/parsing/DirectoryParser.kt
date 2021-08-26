package com.malinskiy.marathon.vendor.junit4.parsing

import com.malinskiy.marathon.extension.relativePathTo
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileNotFoundException

class DirectoryParser {
    private val logger = MarathonLogging.logger {}
    
    fun findTests(directory: File): List<Test> {
        val result = mutableListOf<Test>()
        visitClasses(directory) { directory, file ->
            file.inputStream().use {
                val classReader = ClassReader(it)
                val visitor = JarClassVisitor(Opcodes.ASM6)
                classReader.accept(visitor, 0)

                val fqcn = directory.fqcn
                val pkg = fqcn.substringBeforeLast('.')
                val className = fqcn.substringAfterLast('.')

                result.addAll(
                    visitor.methods
                        .filter { it.annotations.contains("Lorg/junit/Test;") }
                        .mapNotNull { method ->
                            if (visitor.abstract) {
                                logger.warn { "Abstract test class $fqcn" }
                                null
                            } else {
                                Test(
                                    pkg = pkg,
                                    clazz = className,
                                    method = method.name,
                                    metaProperties = emptySet()
                                )
                            }
                        }
                )
            }
        }
        return result.toList()
    }

    private fun <T> visitClasses(directory: File, classVisitor: (String, File) -> T) {
        if (!directory.exists()) throw FileNotFoundException("Directory $directory doesn't exit")
        if (!directory.isDirectory) throw RuntimeException("File $directory is not a directory")

        for (entry in directory.walkTopDown()) {
            when {
                entry.isDirectory -> continue
                entry.name.endsWith(".class") -> {
                    classVisitor(entry.relativePathTo(directory), entry)
                }
            }
        }
    }
}

private val String.fqcn
    get() = replace(File.separatorChar, '.').removeSuffix(".class")
