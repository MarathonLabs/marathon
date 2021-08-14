package com.malinskiy.marathon.vendor.junit4.client

import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.vendor.junit4.parser.contract.DiscoverEvent
import com.malinskiy.marathon.vendor.junit4.parser.contract.DiscoverRequest
import com.malinskiy.marathon.vendor.junit4.parser.contract.TestParserGrpcKt
import io.grpc.ManagedChannel
import kotlinx.coroutines.flow.collect
import java.io.Closeable
import java.io.File
import java.util.concurrent.TimeUnit

class TestDiscoveryClient(
    private val channel: ManagedChannel
) : Closeable {
    private val stub: TestParserGrpcKt.TestParserCoroutineStub =
        TestParserGrpcKt.TestParserCoroutineStub(channel)
            .withWaitForReady()
            .withMaxInboundMessageSize((32 * 1e6).toInt())
            .withMaxOutboundMessageSize((32 * 1e6).toInt())

    suspend fun execute(rootPackage: String, applicationClasspath: List<File>, testClasspath: List<File>): List<Test> {
        val request = DiscoverRequest.newBuilder()
            .addAllApplicationClasspath(applicationClasspath.map { it.absolutePath })
            .addAllTestClasspath(testClasspath.map { it.absolutePath })
            .setRootPackage(rootPackage)
            .build()

        val responseFlow = stub.execute(request)
        val response = mutableListOf<Test>()
        responseFlow.collect { event: DiscoverEvent ->
            when (event.eventType) {
                com.malinskiy.marathon.vendor.junit4.parser.contract.EventType.PARTIAL_PARSE -> {
                    val tests: List<Test> = event.testList.map { description ->
                        Test(
                            description.pkg,
                            description.clazz,
                            description.method,
                            description.metaPropertiesList.map { MetaProperty(it) })
                    }
                    response.addAll(tests)
                }
                com.malinskiy.marathon.vendor.junit4.parser.contract.EventType.FINISHED -> Unit
                com.malinskiy.marathon.vendor.junit4.parser.contract.EventType.UNRECOGNIZED -> Unit
            }
        }
        return response
    }

    override fun close() {
        channel.shutdown().awaitTermination(10, TimeUnit.SECONDS)
    }
}
