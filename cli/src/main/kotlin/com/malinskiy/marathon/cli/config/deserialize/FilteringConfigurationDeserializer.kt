//package com.malinskiy.marathon.cli.config.deserialize
//
//import com.fasterxml.jackson.core.JsonParser
//import com.fasterxml.jackson.databind.DeserializationContext
//import com.fasterxml.jackson.databind.JsonNode
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.databind.deser.std.StdDeserializer
//import com.fasterxml.jackson.databind.node.ArrayNode
//import com.fasterxml.jackson.databind.node.ObjectNode
//import com.fasterxml.jackson.module.kotlin.treeToValue
//import com.malinskiy.marathon.execution.FilteringConfiguration
//import com.malinskiy.marathon.execution.TestFilter
//import com.malinskiy.marathon.execution.strategy.SortingStrategy
//import com.malinskiy.marathon.execution.strategy.impl.pooling.OmniPoolingStrategy
//import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.AbiPoolingStrategy
//import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ManufacturerPoolingStrategy
//import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ModelPoolingStrategy
//import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.OperatingSystemVersionPoolingStrategy
//import com.malinskiy.marathon.execution.strategy.impl.sorting.ExecutionTimeSortingStrategy
//import com.malinskiy.marathon.execution.strategy.impl.sorting.NoSortingStrategy
//import com.malinskiy.marathon.execution.strategy.impl.sorting.SuccessRateSortingStrategy
//
//class FilteringConfigurationDeserializer : StdDeserializer<FilteringConfiguration>(FilteringConfiguration::class.java) {
//    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): FilteringConfiguration {
//        val codec = p?.codec as ObjectMapper
//        val node: JsonNode = codec.readTree(p) ?: throw RuntimeException("Missing filtering configuration")
//
//        val blacklist = mutableListOf<TestFilter>()
//        val whitelist = mutableListOf<TestFilter>()
//
//
//        val blacklistNode = node.get("blacklist")
//        val whitelistNode = node.get("whitelist")
//
//
//        val arrayNode = node as ArrayNode
//        arrayNode.forEach {
//            val type = it.get("type").asText()
//            list.add(
//                    when (type) {
//                        "omni" -> OmniPoolingStrategy()
//                        "device-model" -> ModelPoolingStrategy()
//                        "os-version" -> OperatingSystemVersionPoolingStrategy()
//                        "manufacturer" -> ManufacturerPoolingStrategy()
//                        "abi" -> AbiPoolingStrategy()
//                        else -> throw RuntimeException("Unrecognized pooling strategy ${type}")
//                    }
//            )
//        }
//    }
//}