package com.malinskiy.marathon.cache.gradle

import com.malinskiy.marathon.cache.CacheEntryReader
import com.malinskiy.marathon.cache.CacheEntryWriter
import com.malinskiy.marathon.cache.CacheKey
import com.malinskiy.marathon.cache.CacheService
import com.malinskiy.marathon.cache.config.RemoteCacheConfiguration
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import kotlinx.coroutines.io.ByteWriteChannel
import java.io.IOException
import java.net.URI
import java.net.URL

class GradleHttpCacheService(private val configuration: RemoteCacheConfiguration.Enabled) : CacheService {

    private val httpClient = createClient()
    private val baseUri = URI.create(configuration.url)

    override suspend fun load(key: CacheKey, reader: CacheEntryReader): Boolean =
        try {
            val response = httpClient.get<HttpResponse>(url = key.entryUrl())
            if (response.status != HttpStatusCode.OK) {
                false
            } else {
                reader.readFrom(response.content)
                true
            }
        } catch (exception: IOException) {
            false
        }

    override suspend fun store(key: CacheKey, writer: CacheEntryWriter) {
        try {
            httpClient.put<HttpResponse>(url = key.entryUrl()) {
                body = object : OutgoingContent.WriteChannelContent() {
                    override suspend fun writeTo(channel: ByteWriteChannel) {
                        writer.writeTo(channel)
                    }
                }
            }
        } catch (exception: IOException) {
            // ignore
        }
    }

    override fun close() {
        httpClient.close()
    }

    private fun CacheKey.entryUrl(): URL =
        baseUri.resolve(this.key).toURL()

    private fun createClient(): HttpClient = HttpClient(Apache) {
        engine {
            followRedirects = true
        }

        expectSuccess = false

        configuration.credentials?.let { credentials ->
            install(Auth) {
                basic {
                    username = credentials.userName
                    password = credentials.password
                }
            }
        }
    }
}
