package com.malinskiy.marathon.android.extension

import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import java.io.File

/**
 * ktor by default doesn't flush or close the channel when you call close
 *
 * see https://youtrack.jetbrains.com/issue/KTOR-1618
 */
suspend fun File.writeAsynchronously(dispatcher: CoroutineDispatcher, function: suspend (ByteWriteChannel) -> Unit) {
    val job = Job()
    val channel = writeChannel(dispatcher + job)
    function(channel)
    channel.close()
    job.complete()
    job.join()
}
