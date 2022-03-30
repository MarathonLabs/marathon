package com.malinskiy.marathon.analytics.external.influx2

import com.influxdb.LogLevel
import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.InfluxDBClientOptions
import com.influxdb.client.domain.BucketRetentionRules
import com.malinskiy.marathon.config.AnalyticsConfiguration
import com.malinskiy.marathon.log.MarathonLogging
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

const val TIMEOUT_SEC = 60L

class InfluxDb2Provider(configuration: AnalyticsConfiguration.InfluxDb2Configuration) {
    private val logger = MarathonLogging.logger {}

    private val url = configuration.url
    private val token = configuration.token
    private val org = configuration.organization
    private val bucket = configuration.bucket
    private val retentionPolicyConfiguration = configuration.retentionPolicyConfiguration

    fun createDb(): InfluxDBClient {
        val okHttpBuilder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)

        val options = InfluxDBClientOptions.builder().apply {
            authenticateToken(token.toCharArray())
            url(url)
            org(org)
            bucket(bucket)
            okHttpClient(okHttpBuilder)
            logLevel(LogLevel.BASIC)
        }.build()

        val influxDBClient = InfluxDBClientFactory.create(options)
        val organizations = influxDBClient.organizationsApi.findOrganizations()
        val orgCandidates = organizations.filter { it.name == org }
        when {
            orgCandidates.size > 1 -> logger.warn { "Multiple organizations with name = $org found. Using first one, id = ${orgCandidates.first().id}" }
            orgCandidates.isEmpty() -> {
                throw Influxdb2Exception("No organization with name = $org found")
            }
        }
        
        val organizationByID = orgCandidates.first()
        if (influxDBClient.bucketsApi.findBucketByName(bucket) == null) {
            logger.info {
                "Creating InfluxDB 2 bucket: orgId = ${organizationByID.id}, bucket = ${bucket}, retention: $retentionPolicyConfiguration"
            }

            val bucketRetentionRules = BucketRetentionRules().apply {
                everySeconds(retentionPolicyConfiguration.everySeconds)
                shardGroupDurationSeconds(retentionPolicyConfiguration.shardGroupDurationSeconds)
            }

            influxDBClient.bucketsApi.createBucket(bucket, bucketRetentionRules, organizationByID)
        }

        return influxDBClient
    }
}
