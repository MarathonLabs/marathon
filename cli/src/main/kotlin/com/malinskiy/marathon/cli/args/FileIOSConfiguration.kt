package com.malinskiy.marathon.cli.args

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.ios.IOSConfiguration
import java.io.File

interface FileListProvider {
    fun fileList(root: File = File(".")): Iterable<File>
}

object DerivedDataFileListProvider: FileListProvider {
    override fun fileList(root: File): Iterable<File> {
        return root.walkTopDown().asIterable()
    }
}



data class FileIOSConfiguration(
        @JsonProperty("derivedDataDir") val derivedDataDir: File,
        @JsonProperty("xctestrunPath") val xctestrunPath: File?,
        @JsonProperty("remoteUsername") val remoteUsername: String,
        @JsonProperty("remotePrivateKey") val remotePrivateKey: File,
        @JsonProperty("knownHostsPath") val knownHostsPath: File?,
        @JsonProperty("remoteRsyncPath") val remoteRsyncPath: String = "/usr/bin/rsync",
        @JsonProperty("sourceRoot") val sourceRoot: File?,
        @JsonProperty("sourceTargetName") val sourceTargetName: String?,
        @JsonProperty("binaryParserDockerImageName") val binaryParserDockerImageName: String?,
        @JsonProperty("alwaysEraseSimulators") val alwaysEraseSimulators: Boolean?,
        @JsonProperty("debugSsh") val debugSsh: Boolean?,
        @JsonProperty("hideRunnerOutput") val hideRunnerOutput: Boolean?,
        @JsonProperty("compactOutput") val compactOutput: Boolean = false,
        @JsonProperty("keepAliveIntervalMillis") val keepAliveIntervalMillis: Long = 0L,
        @JsonProperty("deviceInitializationTimeoutMillis") val deviceInitializationTimeoutMillis: Long?,
        @JsonProperty("devices") val devices: File?,
        val fileListProvider: FileListProvider = DerivedDataFileListProvider) : FileVendorConfiguration {

    fun toIOSConfiguration(marathonfileDir: File,
                           sourceRootOverride: File? = null): IOSConfiguration {
        // Any relative path specified in Marathonfile should be resolved against the directory Marathonfile is in
        val resolvedDerivedDataDir = marathonfileDir.resolve(derivedDataDir)
        val finalXCTestRunPath = xctestrunPath?.resolveAgainst(marathonfileDir)
                ?: fileListProvider
                        .fileList(resolvedDerivedDataDir)
                        .firstOrNull { it.extension == "xctestrun" }
                ?: throw ConfigurationException("Unable to find an xctestrun file in derived data folder")
        val optionalSourceRoot = sourceRootOverride
                ?: sourceRoot?.resolveAgainst(marathonfileDir)
        val optionalDebugSsh = debugSsh ?: false
        val optionalAlwaysEraseSimulators = alwaysEraseSimulators ?: true
        val optionalDevices = devices?.resolveAgainst(marathonfileDir)
                ?: marathonfileDir.resolve("Marathondevices")
        val optionalKnownHostsPath = knownHostsPath?.resolveAgainst(marathonfileDir)
        val optionalHideRunnerOutput = hideRunnerOutput ?: false
        val optionalDeviceInitializationTimeoutMillis =
                deviceInitializationTimeoutMillis ?: IOSConfiguration.DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS

        return if (optionalSourceRoot == null) {
            IOSConfiguration(
                    derivedDataDir = resolvedDerivedDataDir,
                    xctestrunPath = finalXCTestRunPath,
                    remoteUsername = remoteUsername,
                    remotePrivateKey = remotePrivateKey,
                    knownHostsPath = optionalKnownHostsPath,
                    remoteRsyncPath = remoteRsyncPath,
                    debugSsh = optionalDebugSsh,
                    alwaysEraseSimulators = optionalAlwaysEraseSimulators,
                    hideRunnerOutput = optionalHideRunnerOutput,
                    compactOutput = compactOutput,
                    keepAliveIntervalMillis = keepAliveIntervalMillis,
                    deviceInitializationTimeoutMillis = optionalDeviceInitializationTimeoutMillis,
                    devicesFile = optionalDevices,
                    sourceTargetName = sourceTargetName,
                    binaryParserDockerImageName = binaryParserDockerImageName)
        } else {
            IOSConfiguration(
                    derivedDataDir = resolvedDerivedDataDir,
                    xctestrunPath = finalXCTestRunPath,
                    remoteUsername = remoteUsername,
                    remotePrivateKey = remotePrivateKey,
                    knownHostsPath = optionalKnownHostsPath,
                    remoteRsyncPath = remoteRsyncPath,
                    debugSsh = optionalDebugSsh,
                    alwaysEraseSimulators = optionalAlwaysEraseSimulators,
                    hideRunnerOutput = optionalHideRunnerOutput,
                    compactOutput = compactOutput,
                    keepAliveIntervalMillis = keepAliveIntervalMillis,
                    deviceInitializationTimeoutMillis = optionalDeviceInitializationTimeoutMillis,
                    devicesFile = optionalDevices,
                    sourceRoot = optionalSourceRoot,
                    sourceTargetName = sourceTargetName,
                    binaryParserDockerImageName = binaryParserDockerImageName)
        }
    }
}

// inverted [resolve] call allows to avoid too many if expressions
private fun File.resolveAgainst(file: File): File = file.resolve(this)
