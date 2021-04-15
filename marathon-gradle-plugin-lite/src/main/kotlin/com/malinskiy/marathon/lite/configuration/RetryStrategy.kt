package com.malinskiy.marathon.lite.configuration

import java.io.Serializable
import com.malinskiy.marathon.cliconfig.proto.RetryStrategy as ProtoRetryStrategy

sealed class RetryStrategy : Serializable {
    object Disabled : RetryStrategy()
    data class FixedQuota(
        val totalAllowedRetryQuota: Int = 200,
        val retryPerTestQuota: Int = 3
    ) : RetryStrategy()
}


fun RetryStrategy.toProto(): ProtoRetryStrategy {
    val builder = ProtoRetryStrategy.newBuilder()
    when (this) {
        RetryStrategy.Disabled -> builder.disabled = ProtoRetryStrategy.Disabled.getDefaultInstance()
        is RetryStrategy.FixedQuota -> {
            val fixedQuota = ProtoRetryStrategy.FixedQuota.newBuilder()
            fixedQuota.totalAllowedRetryQuota = totalAllowedRetryQuota
            fixedQuota.retryPerTestQuota = retryPerTestQuota
            builder.fixedQuota = fixedQuota.build()
        }
    }
    return builder.build()
}
