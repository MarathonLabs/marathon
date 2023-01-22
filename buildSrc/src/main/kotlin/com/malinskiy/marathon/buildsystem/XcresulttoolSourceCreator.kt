package com.malinskiy.marathon.buildsystem

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.malinskiy.marathon.buildsystem.xcresulttool.Schema
import com.malinskiy.marathon.buildsystem.xcresulttool.TypeDefinition
import com.malinskiy.marathon.buildsystem.xcresulttool.Property
import java.io.File
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.kotlin.gradle.utils.`is`

class XcresulttoolSourceCreator(private val schema: File, private val pkg: String) {
    /**
     * This type is reference in the schema but not defined properly as of 3.39
     */
    private val filteredParameterTypes = setOf("SchemaSerializable")
    private val filterInternal = true

    /**
     * Type inheritance is a very complicated feature for simple structs
     * We replace the inheritance with flat data classes
     */
    fun createKotlinSource(generatedDirectory: File) {
        val json = schema.readText()
        var schema = Gson().fromJson(json, Schema::class.java)

        val objects = mutableListOf<TypeDefinition>()
        val values = mutableListOf<TypeDefinition>()
        val arrays = mutableListOf<TypeDefinition>()
        schema.types.forEach {
            when (it.kind) {
                "object" -> objects.add(it)
                "value" -> values.add(it)
                "array" -> arrays.add(it)
                else -> throw RuntimeException("Unknown xcresulttool type kind ${it.kind}")
            }
        }

        var processed = mutableMapOf<String, List<PropertySpec>>()

        println("Starting generation of " + objects.joinToString { it.type.name })
        var (currentIterationObjects, objectsToGenerate) = objects.partition { canGenerate(it, processed) }
        while (currentIterationObjects.isNotEmpty()) {
            println("Generating ${currentIterationObjects.map { it.type.name }.joinToString()}")
            currentIterationObjects.forEach { typeDefinition ->
                val properties = typeDefinition.properties
                    .filter { !filteredParameterTypes.contains(it.type) }
                    .filter { filterInternal && !it.isInternal }

                var propertySpecs = properties.map { property ->
                    PropertySpec.builder(
                        property.name,
                        property.unwrappedType(pkg)
                    )
                        .addAnnotation(
                            AnnotationSpec.builder(
                                ClassName("com.fasterxml.jackson.annotation", "JsonProperty")
                            ).addMember("value=\"${property.name}\"", "")
                                .build()
                        )
                        .build()
                }
                propertySpecs = if (typeDefinition.type.supertype != null) {
                    propertySpecs + processed[typeDefinition.type.supertype]!!
                } else {
                    propertySpecs
                }
                val typeSpec = TypeSpec.classBuilder(typeDefinition.type.name)
                    .addModifiers(KModifier.DATA)
                    .primaryConstructor(propertySpecs)
                    .build()

                FileSpec.builder(pkg, typeDefinition.type.name).addType(typeSpec).build().writeTo(generatedDirectory)
                println("Generated ${typeDefinition.type.name}")

                processed[typeDefinition.type.name] = propertySpecs
            }

            val (nextIterationObjects, leftoGenerate) = objectsToGenerate.partition { canGenerate(it, processed) }
            currentIterationObjects = nextIterationObjects
            objectsToGenerate = leftoGenerate
        }


        if (objectsToGenerate.size != 0) {
            throw RuntimeException("Can't resolve types ${objectsToGenerate.joinToString { it.type.name }}")
        }
    }

    fun canGenerate(typeDefinition: TypeDefinition, known: Map<String, List<PropertySpec>>): Boolean {
        val dependencies = hashSetOf<String>().apply {
            typeDefinition.type.supertype?.let { add(it) }
            typeDefinition.properties
                .filter { !filteredParameterTypes.contains(it.type) }
                .filter { filterInternal && !it.isInternal }
                .forEach {
                    add(
                        if (it.isOptional) {
                            it.wrappedType!!
                        } else if (it.type == "Array") {
                            it.wrappedType!!
                        } else {
                            it.type
                        }
                    )
                }
        }
        if (typeDefinition.type.name == "ActionTestPlanRunSummaries") {
            dependencies.forEach {
                println(it + ":${known.containsKey(it) || Property.WKT.containsKey(it)}")
            }
        }

        return dependencies.all {
            known.containsKey(it) || Property.WKT.containsKey(it) || Property.ARTIFICIAL_TYPES.contains(it) ||
                it == typeDefinition.type.name //Type loop
        }
    }
}


fun TypeSpec.Builder.primaryConstructor(properties: List<PropertySpec>): TypeSpec.Builder {
    val propertySpecs = properties.map { p -> p.toBuilder().initializer(p.name).build() }
    val parameters = propertySpecs.map {
        ParameterSpec
            .builder(it.name, it.type)
            .apply {
                //Apple decided that it's ok to have non-optional arrays have null values
                (it.type as? ParameterizedTypeName)?.let { parameterizedTypeName ->
                    if (parameterizedTypeName.rawType == Property.LIST && !parameterizedTypeName.isNullable) {
                        defaultValue("emptyList()")
                    }
                }
                /**
                 * According to what Apple believes is sane json spec, some types like Integer marked as non-optional should still be optional
                 * and default to some unspecified value
                 */
                if (!it.type.isNullable) {
                    when (it.type) {
                        Int::class.asTypeName() -> {
                            defaultValue("0")
                        }
                        Double::class.asTypeName() -> {
                            defaultValue(".0")
                        }
                        Boolean::class.asTypeName() -> {
                            defaultValue("false")
                        }
                        String::class.asTypeName() -> {
                            defaultValue("\"\"")
                        }
                    }
                }
            }
            .build()
    }
    val constructor = FunSpec.constructorBuilder()
        .addParameters(parameters)
        .build()

    return this
        .primaryConstructor(constructor)
        .addProperties(propertySpecs)
}
