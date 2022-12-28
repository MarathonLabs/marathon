package com.malinskiy.marathon.config.serialization.yaml

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.malinskiy.marathon.config.environment.EnvironmentReader
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.ios.AppleTestBundleConfiguration
import com.malinskiy.marathon.config.vendor.ios.SshAuthentication
import java.io.File

const val TYPE_ANDROID = "Android"
const val TYPE_IOS = "iOS"

class VendorConfigurationDeserializer(
    private val marathonfileDir: File,
    private val environmentReader: EnvironmentReader,
    private val fileListProvider: FileListProvider,
) :
    StdDeserializer<VendorConfiguration>(VendorConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): VendorConfiguration {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Missing vendor configuration")
        val type = node.get("type").asText()

        return when (type) {
            TYPE_IOS -> {
                (node as ObjectNode).remove("type")
                val iosConfiguration = codec.treeToValue<VendorConfiguration.IOSConfiguration>(node)
                    ?: throw ConfigurationException("Missing vendor configuration")

                // Any relative path specified in Marathonfile should be resolved against the directory Marathonfile is in
                val resolvedBundle = iosConfiguration.bundle?.let {
                    val resolvedDerivedDataDir = it.derivedDataDir?.let { ddd -> marathonfileDir.resolve(ddd) }
                    val resolvedApplication = it.application?.let { ddd -> marathonfileDir.resolve(ddd) }
                    val resolvedTestApplication = it.testApplication?.let { ddd -> marathonfileDir.resolve(ddd) }
                    AppleTestBundleConfiguration(resolvedApplication, resolvedTestApplication, resolvedDerivedDataDir)
                }
                val optionalDevices = iosConfiguration.devicesFile?.resolveAgainst(marathonfileDir)
                    ?: marathonfileDir.resolve("Marathondevices")

                val optionalknownHostsPath = iosConfiguration.ssh.knownHostsPath?.resolveAgainst(marathonfileDir)
                val optionalSshAuthentication = when (iosConfiguration.ssh.authentication) {
                    is SshAuthentication.PasswordAuthentication -> iosConfiguration.ssh.authentication
                    is SshAuthentication.PublicKeyAuthentication -> iosConfiguration.ssh.authentication.copy(
                        username = iosConfiguration.ssh.authentication.username,
                        key = iosConfiguration.ssh.authentication.key.resolveAgainst(marathonfileDir)
                    )

                    null -> null
                }
                val optionalSshConfiguration = iosConfiguration.ssh.copy(
                    authentication = optionalSshAuthentication,
                    knownHostsPath = optionalknownHostsPath,
                )

                iosConfiguration.copy(
                    bundle = resolvedBundle,
                    devicesFile = optionalDevices,
                    ssh = optionalSshConfiguration,
                )
            }

            TYPE_ANDROID -> {
                (node as ObjectNode).remove("type")
                var androidConfiguration = codec.treeToValue<VendorConfiguration.AndroidConfiguration>(node)
                    ?: throw ConfigurationException("Missing vendor configuration")
                if (androidConfiguration.androidSdk == null) {
                    val androidSdk = environmentReader.read().androidSdk ?: throw ConfigurationException("No android SDK path specified")
                    androidConfiguration.copy(
                        androidSdk = androidSdk
                    )
                } else {
                    androidConfiguration
                }
            }

            else -> throw ConfigurationException(
                "Unrecognized vendor type $type. " +
                    "Valid options are $TYPE_ANDROID and $TYPE_IOS"
            )
        }
    }
}

interface FileListProvider {
    fun fileList(root: File = File(".")): Iterable<File>
}

object DerivedDataFileListProvider : FileListProvider {
    override fun fileList(root: File): Iterable<File> {
        return root.walkTopDown().asIterable()
    }
}


// inverted [resolve] call allows to avoid too many if expressions
private fun File.resolveAgainst(file: File): File = file.resolve(this).canonicalFile
