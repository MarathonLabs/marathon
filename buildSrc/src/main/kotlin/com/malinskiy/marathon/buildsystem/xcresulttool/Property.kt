package com.malinskiy.marathon.buildsystem.xcresulttool

import com.google.gson.annotations.SerializedName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import java.util.Date
import kotlin.reflect.KClass
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

data class Property(
    @SerializedName("isInternal") val isInternal: Boolean,
    @SerializedName("isOptional") val isOptional: Boolean,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("wrappedType") val wrappedType: String?,
) {
    fun unwrappedType(pkg: String): TypeName {
        val unwrappedType = if (isOptional) {
            wrappedType!!
        } else if (type == "Array") {
            wrappedType!!
        } else {
            type
        }

        return (WKT[unwrappedType] ?: ClassName(pkg, unwrappedType))
            .let { parameterType ->
                if(type == "Array") {
                    LIST.parameterizedBy(parameterType)
                } else {
                    parameterType
                }
            }
            .let { mappedType ->
            if (isOptional) {
                mappedType.copy(nullable = true)
            } else {
                mappedType
            }
        }
    }
    

    companion object {
        val WKT = mutableMapOf<String, TypeName>().apply {
            put("Date", Date::class.asClassName())
            put("Double", Double::class.asClassName())
            put("Int", Int::class.asClassName())
            put("String", String::class.asClassName())
            put("Bool", Boolean::class.asClassName())
        }
        val ARTIFICIAL_TYPES = setOf("Reference")
        val LIST = ClassName("kotlin.collections", "List")
    }
}
