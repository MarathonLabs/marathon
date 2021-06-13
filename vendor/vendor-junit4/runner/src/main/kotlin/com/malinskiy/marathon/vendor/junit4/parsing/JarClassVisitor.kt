package com.malinskiy.marathon.vendor.junit4.parsing

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import java.lang.reflect.Modifier.isAbstract

class JarClassVisitor(api: Int) : ClassVisitor(api) {
    val methods: MutableList<JarMethodVisitor> = mutableListOf()
    var abstract: Boolean = false

    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        super.visit(version, access, name, signature, superName, interfaces)
        abstract = isAbstract(access)
    }

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
