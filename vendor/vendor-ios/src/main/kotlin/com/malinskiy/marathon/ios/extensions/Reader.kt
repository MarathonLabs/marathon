package com.malinskiy.marathon.ios.extensions

import com.malinskiy.marathon.ios.cmd.LineBuffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.time.Duration
import java.util.concurrent.TimeoutException
import kotlin.math.min

fun CoroutineScope.produceLines(
    job: Job,
    inputStream: InputStream,
    idleTimeout: Duration, //TODO: underlying api for reading doesn't support timeout in the iterator and in the readLine call
    charset: Charset,
    channelCapacity: Int,
    canRead: () -> Boolean,
): ReceiveChannel<String> {
    return produce(capacity = channelCapacity, context = job) {
        coroutineContext[Job]?.invokeOnCompletion { inputStream.close() }
        inputStream.bufferedReader(charset).use {
            it.lineSequence().forEach { line ->
                if (channel.isClosedForSend || job.isCancelled) {
                    return@use
                }
                channel.send(line)
            }
        }
    }
}

fun CoroutineScope.produceLinesManually(
    job: Job,
    inputStream: InputStream,
    idleTimeout: Duration,
    charset: Charset,
    channelCapacity: Int,
    canRead: () -> Boolean,
): ReceiveChannel<String> {
    return produce(capacity = channelCapacity, context = job) {
        inputStream.buffered().use { inputStream ->

            var lastOutputTimeMillis = System.currentTimeMillis()
            LineBuffer(charset, onLine = { send(it) }).use { lineBuffer ->
                val byteArray = ByteArray(16384)
                while (coroutineContext.isActive && !channel.isClosedForSend && !job.isCancelled) {
                    val available = try {
                        inputStream.available()
                    } catch (e: IOException) {
                        break
                    }
                    // available value is expected to indicate an estimated number of bytes
                    // that can be read without blocking (actual count may be smaller).
                    //
                    // when requesting a zero length, reading from sshj's [ChannelInputStream]
                    // blocks. to accurately handle no output timeout, check if session has
                    // received EOF.
                    //
                    val count = when {
                        available > 0 -> inputStream.read(byteArray, 0, min(available, byteArray.size))
                        else -> 0
                    }

                    val timeSinceLastOutputMillis = System.currentTimeMillis() - lastOutputTimeMillis
                    if (timeSinceLastOutputMillis > idleTimeout.toMillis()) {
                        close(TimeoutException("idle timeout $idleTimeout reached"))
                        break
                    }

                    // if there was nothing to read
                    if (count == 0) {
                        // if session received EOF or has been closed, reading stops
                        if (!canRead()) {
                            break
                        }
                        // sleep for a moment
                        delay(SLEEP_DURATION_MILLIS)
                    } else if (count == -1) {
                        break
                    } else {
                        lineBuffer.append(byteArray, count)
                        lastOutputTimeMillis = System.currentTimeMillis()
                    }
                    // immediately send any full lines for parsing
                    lineBuffer.flush()
                }
                // immediately send any full lines for parsing
                lineBuffer.drain()
            }
        }
    }
}

private const val SLEEP_DURATION_MILLIS = 1L
