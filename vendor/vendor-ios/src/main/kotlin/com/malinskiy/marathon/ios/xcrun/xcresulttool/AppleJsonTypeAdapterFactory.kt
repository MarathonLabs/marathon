package com.malinskiy.marathon.ios.xcrun.xcresulttool

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class AppleJsonTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T> {
        val rawType = type.rawType as Class<T>
        if(rawType is List<*>) {
            println("")
        }
        
        val actualAdapter = gson.getDelegateAdapter(this, type)
        
        return when (rawType)  {
            java.util.Date::class.java -> AppleJsonTypeAdapter(actualAdapter)
            Double::class.java, java.lang.Double::class.java -> AppleJsonTypeAdapter(actualAdapter)
            Int::class.java, java.lang.Integer::class.java -> AppleJsonTypeAdapter(actualAdapter)
            String::class.java, java.lang.String::class.java -> AppleJsonTypeAdapter(actualAdapter)
            Boolean::class.java, java.lang.Boolean::class.java -> AppleJsonTypeAdapter(actualAdapter)
            else -> actualAdapter
        }
    }
}

class AppleJsonTypeAdapter<T>(private val actualAdapter: TypeAdapter<T>) : TypeAdapter<T>() {
    override fun write(out: JsonWriter?, value: T) {
        TODO("We shouldn't write this level of insanity")
    }

    override fun read(reader: JsonReader): T? {
        var result: T? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "_type" -> {
                    reader.skipValue()
                }
                "_value" -> {
                    result = actualAdapter.read(reader)
                }
                "_values" -> {
                    result = actualAdapter.read(reader)
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return result
    }
}

internal class AppleListConverter : JsonDeserializer<List<*>?> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): List<*> {
        val valueType: Type = (typeOfT as ParameterizedType).actualTypeArguments[0]
        val list = mutableListOf<Any>()
        for (item in json.asJsonObject.get("_values").asJsonArray) {
            list.add(ctx.deserialize(item, valueType))
        }
        return list
    }
}
