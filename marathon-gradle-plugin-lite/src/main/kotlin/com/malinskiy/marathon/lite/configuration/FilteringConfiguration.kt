package com.malinskiy.marathon.lite.configuration

import java.io.Serializable
import com.malinskiy.marathon.cliconfig.proto.FilteringConfiguration as ProtoFilteringConfiguration

data class FilteringConfiguration(
    val allowList: List<TestFilter> = emptyList(),
    val blockList: List<TestFilter> = emptyList()
) : Serializable

fun FilteringConfiguration.toProto(): ProtoFilteringConfiguration {
    val builder = ProtoFilteringConfiguration.newBuilder()
    builder.addAllAllowList(allowList.map { it.toProto() })
    builder.addAllAllowList(blockList.map { it.toProto() })
    return builder.build()
}
