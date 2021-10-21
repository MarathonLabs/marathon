package com.malinskiy.marathon.config.serialization.yaml

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class RegexSerializer : StdSerializer<Regex>(Regex::class.java) {
    override fun serialize(value: Regex, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.pattern)
    }
}
