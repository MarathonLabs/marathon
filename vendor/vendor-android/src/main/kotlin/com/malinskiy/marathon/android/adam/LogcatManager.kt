package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.request.logcat.ChanneledLogcatRequest
import com.malinskiy.adam.request.logcat.LogcatReadMode
import com.malinskiy.marathon.android.adam.log.LogCatMessageParser
import com.malinskiy.marathon.coroutines.newCoroutineExceptionHandler
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.supervisorScope
import java.util.concurrent.ConcurrentHashMap

class LogcatManager : CoroutineScope {
    private val logger = MarathonLogging.logger {}

    private val dispatcher = newFixedThreadPoolContext(4, "LogcatManager")
    override val coroutineContext = dispatcher + newCoroutineExceptionHandler(logger)

    private val devices: ConcurrentHashMap<AdamAndroidDevice, Job> = ConcurrentHashMap()

    fun close() {
        dispatcher.close()
    }

    fun subscribe(device: AdamAndroidDevice) {
        devices[device] = launch {
            supervisorScope {
                val logcatChannel = device.client.execute(
                    ChanneledLogcatRequest(
                        modes = listOf(LogcatReadMode.long)
                    ), serial = device.adbSerial, scope = this
                )

                val parser = LogCatMessageParser()
                for (logPart in logcatChannel) {
                    val messages = parser.processLogLines(logPart.lines(), device)
                    messages.forEach { msg -> device.onLogcat(msg) }
                }
            }
        }
    }

    fun unsubscribe(device: AdamAndroidDevice) {
        devices[device]?.cancel()
        devices.remove(device)
    }
}
