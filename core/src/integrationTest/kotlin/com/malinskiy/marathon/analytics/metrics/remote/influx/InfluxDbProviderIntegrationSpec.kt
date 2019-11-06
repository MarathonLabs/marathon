package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.analytics.external.influx.InfluxDbProvider
import com.malinskiy.marathon.analytics.external.influx.InfluxMetricsProvider
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.generateTest
import org.amshove.kluent.shouldEqualTo
import org.influxdb.InfluxDB
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.time.Instant
import java.time.temporal.ChronoUnit

class InfluxDbProviderIntegrationSpec : Spek(
    {
        val database = "marathonTest"

        val container = KInfluxDBContainer().withAuthEnabled(false)

        var thirdDbInstance: InfluxDB? = null

        beforeGroup {
            container.start()
        }
        afterGroup {
            thirdDbInstance?.close()
            container.stop()
        }

        describe("InfluxDbProvider") {
            group("multiple creations") {
                val test = generateTest()
                it("should still have the same configured retention policy") {
                    val provider = InfluxDbProvider(
                        AnalyticsConfiguration.InfluxDbConfiguration(
                            url = container.url,
                            dbName = database,
                            password = "",
                            user = "root",
                            retentionPolicyConfiguration = AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration.default
                        )
                    )
                    val firstDbInstance = provider.createDb()
                    firstDbInstance.close()


                    val secondDbInstance = provider.createDb()
                    prepareData(secondDbInstance, test)
                    secondDbInstance.close()

                    thirdDbInstance = provider.createDb()

                    val metricsProvider =
                        InfluxMetricsProvider(InfluxDBDataSource(thirdDbInstance!!, database))

                    val result = metricsProvider.executionTime(
                        test,
                        50.0,
                        Instant.now().minus(2, ChronoUnit.DAYS)
                    )
                    result shouldEqualTo 5000.0
                }
            }
        }
    })