package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.request.logcat.ChanneledLogcatRequest
import com.malinskiy.adam.request.logcat.LogcatReadMode
import com.malinskiy.marathon.android.adam.log.LogCatMessageParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.supervisorScope
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

class LogcatManager : CoroutineScope {
    private val dispatcher = newFixedThreadPoolContext(4, "LogcatManager")
    override val coroutineContext: CoroutineContext
        get() = dispatcher

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
                    messages.forEach { msg ->
                        device.onLine("${msg.timestamp} ${msg.pid}-${msg.tid}/${msg.appName} ${msg.logLevel.priorityLetter}/${msg.tag}: ${msg.message}")
                    }
                }
            }
        }
    }

    fun unsubscribe(device: AdamAndroidDevice) {
        devices[device]?.cancel()
        devices.remove(device)
    }
}
