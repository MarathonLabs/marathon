package com.malinskiy.marathon.report.allure.steps
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import io.qameta.allure.model.Status
import java.lang.reflect.Type


class AllureStatusDeserializer : JsonDeserializer<Status> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Status {
        if (json is JsonPrimitive) {
            return Status.fromValue(json.asString)
        } else {
            throw IllegalStateException()
        }
    }

}