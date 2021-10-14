package com.malinskiy.marathon.config.serialization.yaml

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.malinskiy.marathon.config.ScreenRecordingPolicy
import com.malinskiy.marathon.config.exceptions.ConfigurationException

class ScreenRecordingPolicyDeserializer : StdDeserializer<ScreenRecordingPolicy>(ScreenRecordingPolicy::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ScreenRecordingPolicy? {
        val value: String = p?.valueAsString ?: return null
        return when (value) {
            "ON_FAILURE" -> ScreenRecordingPolicy.ON_FAILURE
            "ON_ANY" -> ScreenRecordingPolicy.ON_ANY
            else -> throw ConfigurationException("Unrecognized screen recording policy $value")
        }
    }
}
