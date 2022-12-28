package com.malinskiy.marathon.ios.bin.xcrun.xcresulttool

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration
import com.malinskiy.marathon.ios.cmd.CommandExecutor
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
    /**
     * For the Apple trickery with json we require a custom deserializer
     */
    private val gson = GsonBuilder()
        .registerTypeAdapter(List::class.java, AppleListConverter())
        .registerTypeAdapterFactory(AppleJsonTypeAdapterFactory())
        .create()

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
            gson.fromJson(json, clazz)
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
