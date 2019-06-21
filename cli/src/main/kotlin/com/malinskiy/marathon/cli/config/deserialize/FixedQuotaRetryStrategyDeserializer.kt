package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota.FixedQuotaRetryStrategy
import com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota.TestMatcher
import com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota.TestNameRegexTestMatcher

class FixedQuotaRetryStrategyDeserializer: StdDeserializer<FixedQuotaRetryStrategy>(FixedQuotaRetryStrategy::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): FixedQuotaRetryStrategy {
        val codec = p?.codec as ObjectMapper
        val node: JsonNode = codec.readTree(p) ?: throw ConfigurationException("Invalid sorting strategy")

        val totalAllowedRetryQuota = node.findValue("totalAllowedRetryQuota")?.asInt()
                ?: throw ConfigurationException("Missing totalAllowedRetryQuota value")

        val retryPerTestQuota = node.findValue("retryPerTestQuota")?.asInt()
                ?: throw ConfigurationException("Missing retryPerTestQuota value")

        val testMatchers = mutableListOf<TestMatcher>()
        node.get("noRetryTestMatchers")?.let {
            if (!it.isArray) throw ConfigurationException("noRetryTestMatchers must be an array")
            (it as ArrayNode).forEach { node ->
                    deserializeTestMatcher(node)?.let { matcher ->
                        testMatchers.add(matcher)
                    }
                }
        }

        return FixedQuotaRetryStrategy(
                totalAllowedRetryQuota = totalAllowedRetryQuota,
                retryPerTestQuota = retryPerTestQuota,
                noRetryTestMatchers = testMatchers
        )
    }

    private fun deserializeTestMatcher(node: JsonNode): TestMatcher? {
        val method = node.findValue("method")?.asText()
        if (method != null) {
            val pkg = node.findValue("package")?.asText()
            val clazz = node.findValue("class")?.asText()

            return TestNameRegexTestMatcher(pkg = pkg, clazz = clazz, method = method)
        }
        return null
    }
}
