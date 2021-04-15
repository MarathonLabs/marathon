package com.malinskiy.marathon.lite

import com.google.protobuf.Timestamp
import java.time.Instant

fun Instant.toTimestamp(): Timestamp {
    return Timestamp.newBuilder().setSeconds(this.epochSecond).setNanos(this.nano).build()
}
