package com.malinskiy.marathon.execution.command.parse

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import java.io.File

private val log = MarathonLogging.logger {}

class MarathonTestParseCommand {

    private val mapper: ObjectMapper = ObjectMapper(
        YAMLFactory()
            .disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID)
    ).apply {
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, true)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )
    }

    fun execute(tests: List<Test>, outputFile: File?) {
        val parseResult = ParseCommandResult(tests)
        val res = mapper.writeValueAsString(parseResult)

        log.info { "Parse execute mode. Result" }
        log.info { res }

        // todo write to output file
    }
}
