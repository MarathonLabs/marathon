package com.malinskiy.marathon.execution.command.parse

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import org.jetbrains.annotations.TestOnly
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths.get

@get:TestOnly
internal const val EXTENSION = ".yml"

private val log = MarathonLogging.logger {}

/**
 * Further development when new commands will appear:
 * 1. Create an interface like CommandExecutor with a single method "execute(Command)" where Command is a sealed class.
 * 2. MarathonTestParseCommand should be inherited from described above interface.
 * 3. "outputFile" arg in "execute" method should be a part of Config for this particular class. The config is bringing through a constructor.
 * 4. "tests" arg should be a part of Command sealed class inheritor.
 */

class MarathonTestParseCommand(private val outputDir: File) {

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

    fun execute(tests: List<Test>, outputFileName: String?) {
        val parseResult = ParseCommandResult(tests)
        val res = mapper.writeValueAsString(parseResult)

        log.info { "Parse execute mode. Result" }
        log.info { res }
        if (outputFileName == null) return

        val dirPath = Files.createDirectories(get(outputDir.absolutePath))
        val resultFile = File(dirPath.toFile(), outputFileName + EXTENSION)
        resultFile.writeText(res)
    }
}
