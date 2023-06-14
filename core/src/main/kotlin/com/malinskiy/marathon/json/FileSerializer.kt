package com.malinskiy.marathon.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.io.File
import java.lang.reflect.Type

class FileSerializer : JsonSerializer<File> {
    override fun serialize(src: File, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src.absolutePath)
    }
}
