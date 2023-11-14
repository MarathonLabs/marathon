package com.malinskiy.marathon.usageanalytics.tracker

import com.malinskiy.marathon.usageanalytics.Event
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.Exception
import java.util.UUID

class GrafanaCloud : UsageTracker {
    private val client = OkHttpClient()
    private val key =
        "967310:eyJrIjoiMmEzNWM4ZmI2ZTExNjg1OWJiZDUxOWE3YWU1N2NiMTI5MDRhOTk2ZiIsIm4iOiJtYXJhdGhvbi1vc3MtcHVibGlzaGluZyIsImlkIjo4NDcyNDZ9"
    private val accumulator = mutableListOf<Event>()
    private val tags = mutableMapOf<String, String>()

    override fun trackEvent(event: Event) {
        accumulator.add(event)
    }

    override fun meta(version: String, vendor: String, releaseMode: String) {
        tags["version"] = version
        tags["releaseMode"] = releaseMode
        tags["vendor"] = vendor
        tags["id"] = UUID.randomUUID().toString()
    }

    private fun sendEvents(events: Collection<Event>) {
        val body = convertToJson(events)
        send(body)
    }

    private fun convertToJson(events: Collection<Event>): String {
        return StringBuilder().apply {
            append("[")
            append(
                events.map { event ->
                    when (event) {
                        is Event.Devices ->
                            setOf(Metric(name = "testing.device", value = event.total, tags = tags))

                        is Event.Executed -> {
                            setOf(
                                Metric(name = "testing.duration", value = event.seconds, tags = tags),
                                Metric(name = "testing.flakiness", value = event.flakinessSeconds, tags = tags),
                                Metric(name = "testing.result", value = if (event.success) 1 else 0, tags = tags),
                                Metric(name = "testing.duration.run", value = event.durationSeconds, tags = tags),
                            )
                        }

                        is Event.TestsTotal -> setOf(Metric(name = "testing.test", value = event.total, tags = tags))
                        is Event.TestsRun -> setOf(Metric(name = "testing.executed", value = event.value, tags = tags))
                    }
                }.flatten().joinToString(separator = ",") { it.toJson() }
            )
            append("]")
        }.toString()
    }

    override fun close() {
        sendEvents(accumulator)
    }

    private fun send(body: String) {
        try {
            val request = Request.Builder()
                .url("https://graphite-prod-13-prod-us-east-0.grafana.net/graphite/metrics")
                .header("Authorization", "Bearer $key")
                .post(body.toByteArray().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    //se la vie, we shouldn't handle analytics errors because users don't care about them
                }
            }
        } catch (e: Exception) {
            //se la vie, we shouldn't handle analytics errors because users don't care about them
        }
    }
}
