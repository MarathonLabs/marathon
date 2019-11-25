package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.malinskiy.marathon.cache.config.Credentials
import com.malinskiy.marathon.cache.config.LocalCacheConfiguration
import com.malinskiy.marathon.cache.config.RemoteCacheConfiguration
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.CacheConfiguration
import java.io.File
import java.time.Duration
import java.time.Instant

class CacheConfigurationDeserializer :
    StdDeserializer<CacheConfiguration>(CacheConfiguration::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): CacheConfiguration {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Invalid caching configuration")

        val localConfig: LocalCacheConfiguration = node.findValue("local")
            ?.let { localNode ->
                val directory =
                    localNode.findValue("directory")?.asText() ?: throw ConfigurationException("Local cache directory is not specified")
                val expireDays = localNode.findValue("removeUnusedEntriesAfterDays")?.asInt()
                    ?: throw ConfigurationException("Local cache expiration is not specified")

                LocalCacheConfiguration.Enabled(File(directory), expireDays)
            }
            ?: LocalCacheConfiguration.Disabled

        val remoteConfig: RemoteCacheConfiguration = node.findValue("remote")
            ?.let { localNode ->
                val url =
                    localNode.findValue("url")?.asText() ?: throw ConfigurationException("Remote cache URL is not specified")
                val credentials = localNode.findValue("credentials")?.let {
                    val userName =
                        localNode.findValue("userName")?.asText() ?: throw ConfigurationException("Remote cache username is not specified")
                    val password =
                        localNode.findValue("password")?.asText() ?: throw ConfigurationException("Remote cache password is not specified")
                    Credentials(userName, password)
                }

                RemoteCacheConfiguration.Enabled(url, credentials)
            }
            ?: RemoteCacheConfiguration.Disabled

        return CacheConfiguration(
            local = localConfig,
            remote = remoteConfig
        )
    }
}

private fun Duration.addToInstant(instant: Instant): Instant = instant.plus(this)
private fun <T> ObjectMapper.treeToValueOrNull(node: TreeNode, clazz: Class<T>): T? {
    val result: T
    try {
        result = treeToValue(node, clazz)
    } catch (e: InvalidFormatException) {
        return null
    }
    return result
}
