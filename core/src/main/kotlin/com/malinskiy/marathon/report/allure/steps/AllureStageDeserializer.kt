package com.malinskiy.marathon.report.allure.steps

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import io.qameta.allure.model.Stage
import java.lang.IllegalStateException
import java.lang.reflect.Type


class AllureStageDeserializer : JsonDeserializer<Stage> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Stage {
        if (json is JsonPrimitive) {
            return Stage.fromValue(json.asString)
        } else {
            throw IllegalStateException()
        }
    }

}