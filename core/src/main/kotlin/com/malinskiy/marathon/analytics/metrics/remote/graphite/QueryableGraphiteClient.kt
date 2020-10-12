package com.malinskiy.marathon.analytics.metrics.remote.graphite

import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

private const val TIMEOUT_SEC = 60L

class QueryableGraphiteClient(private val host: String) {

    private val fromFormatter = DateTimeFormatter.ofPattern("HH:mm_yyyyMMdd").withZone(ZoneId.systemDefault())
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
        .build()

    fun query(target: String, from: Instant): List<String> {
        val encodedTarget = URLEncoder.encode(target, StandardCharsets.UTF_8.name())
        val formattedFrom = fromFormatter.format(from)
        val request = Request.Builder()
            .url("http://${host}/render?target=$encodedTarget&format=raw&from=$formattedFrom")
            .build()
        return okHttpClient.newCall(request).execute().use { response ->
            response.body()?.string()
                ?.split('\n')
                ?.filter { it.isNotEmpty() }
                .orEmpty()
        }
    }
}
