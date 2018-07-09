package com.malinskiy.marathon.cli

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.cli.args.CliConfiguration
import com.malinskiy.marathon.cli.args.MarathonArguments
import com.malinskiy.marathon.execution.Configuration
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import mu.KotlinLogging
import org.yaml.snakeyaml.Yaml
import java.io.File

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) = mainBody {


    ArgParser(args).parseInto(::MarathonArguments).run {
        logger.info { "Starting marathon" }

        val defaultConfigFile = File("Marathonfile")
        if(defaultConfigFile.exists()) {
            logger.info { "Found ${defaultConfigFile.absolutePath}" }


            val mapper = ObjectMapper(YAMLFactory()
                    .disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
            mapper.registerModule(KotlinModule())
            mapper.enableDefaultTyping()

            val cliConfiguration = mapper.readValue(defaultConfigFile.bufferedReader(), CliConfiguration::class.java)

//            return Files.newBufferedReader(path).use {
//                mapper.readValue(it, ConfigDto::class.java)
//            }
//
//            val yaml = Yaml()
//            val config = yaml.loadAs(defaultConfigFile.bufferedReader(), CliConfiguration::class.java)

            logger.info { cliConfiguration.toString() }
            logger.info { cliConfiguration.poolingStrategy.javaClass.canonicalName }
        }

//        Configuration(
//                name,
//                outputDir,
//                applicationOutput,
//                testApplicationOutput,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                AndroidConfiguration(androidSdkDir)
//        )
    }
}