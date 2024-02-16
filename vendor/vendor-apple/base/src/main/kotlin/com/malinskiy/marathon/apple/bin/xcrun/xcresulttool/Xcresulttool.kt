package com.malinskiy.marathon.apple.bin.xcrun.xcresulttool

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.gson.JsonSyntaxException
import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.config.vendor.apple.ios.TimeoutConfiguration
import com.malinskiy.marathon.log.MarathonLogging

/**
 * view Xcode result bundle data in a human-readable or machine-parseable format
 * 
 * USAGE: xcresulttool subcommand [options] ...
 *
 * SUBCOMMANDS:
 * export                  Export File or Directory from Result Bundle
 * formatDescription       Result Bundle Format Description
 * get                     Get Result Bundle Object
 * graph                   Print Result Bundle Object Graph
 * merge                   Merge Result Bundles
 * metadata                Result Bundle Metadata
 * version                 XCResultKit Version
 */
class Xcresulttool(
    private val commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) {
    private val logger = MarathonLogging.logger {}
    companion object {
        val AppleJsonMapper: ObjectMapper = ObjectMapper().apply {
            registerModule(
                KotlinModule.Builder()
                    .withReflectionCacheSize(512)
                    .configure(KotlinFeature.NullToEmptyCollection, true)
                    .configure(KotlinFeature.NullToEmptyMap, false)
                    .configure(KotlinFeature.NullIsSameAsDefault, true)
                    .configure(KotlinFeature.SingletonSupport, true)
                    .configure(KotlinFeature.StrictNullChecks, false)
                    .build()
            )
            registerModule(AppleModule())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    /**
     * Get Result Bundle Object
     *
     * @param path The result bundle path
     * @param format The output format [json|raw], default: raw
     * @param id The ID of the object [optional, assumes rootID if not specified]. Note: specifying rootID doesn't work as a default
     * @param version For incomplete result bundles (lacking Info.plist), specify version explicitly [optional, assumes latest version if not specified]
     */
    suspend fun <T> get(clazz: Class<T>, path: String, format: ResultBundleFormat = ResultBundleFormat.JSON, id: String? = null, version: String? = null): T? {
        val args = mutableListOf("xcrun", "xcresulttool", "get", "--format", format.value, "--path", path)
        id?.let {
            args.add("--id")
            args.add(it)
        }
        val result =
            commandExecutor.criticalExecute(timeoutConfiguration.shell, *args.toTypedArray())
        val json = result.combinedStdout.trim()
        return try {
            AppleJsonMapper.readValue(json, clazz)
        } catch (e: JsonSyntaxException) {
            logger.warn(e) { "Invalid syntax in the $path" }
            null
        }
    }

    /**
     * OVERVIEW: Merge Result Bundles
     *
     * OPTIONS:
     *   --output-path   The path to write the merged result bundle.
     *
     * POSITIONAL ARGUMENTS:
     *   input paths     Two or more result bundles to merge.
     */
    suspend fun merge(outputPath: String, paths: List<String>): Boolean {
        val result =
            commandExecutor.criticalExecute(timeoutConfiguration.shell, "xcrun", "xcresulttool", "merge", "--output-path", outputPath, *paths.toTypedArray())
        return result.successful
    }
}

enum class ResultBundleFormat(val value: String) {
    JSON("json"),
    RAW("raw");
}
