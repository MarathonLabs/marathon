@file:OptIn(ExperimentalTime::class)

package marathon.extensions

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

fun Duration.format(): String {
    return toComponents { minutes, seconds, nanoseconds ->
        buildString {
            append(minutes)
            append(":")
            append(seconds)
            append(".")
            val nss = nanoseconds.toString().padStart(9, '0')
            when {
                nanoseconds % 1_000_000 == 0 -> appendRange(nss, 0, 3)
                nanoseconds % 1_000 == 0 -> appendRange(nss, 0, 6)
                else -> append(nss)
            }
        }
    }
}
