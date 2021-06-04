package com.malinskiy.marathon.vendor.junit4.parsing

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class JarClassVisitor(api: Int) : ClassVisitor(api) {
    val methods: MutableList<JarMethodVisitor> = mutableListOf()

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        val jarMethodVisitor = JarMethodVisitor(name, api)
        methods.add(jarMethodVisitor)
        return methodVisitor ?: jarMethodVisitor
    }
}
