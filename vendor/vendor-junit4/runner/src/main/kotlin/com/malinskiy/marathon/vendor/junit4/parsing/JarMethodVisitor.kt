package com.malinskiy.marathon.vendor.junit4.parsing

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor

class JarMethodVisitor(val name: String, api: Int) : MethodVisitor(api) {
    val annotations: MutableList<String> = mutableListOf()

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        annotations.add(descriptor)
        return super.visitAnnotation(descriptor, visible)
    }
}
