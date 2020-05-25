package com.malinskiy.marathon.ios.idb.grpc

import com.google.protobuf.ByteString
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import idb.CompanionServiceGrpcKt
import idb.InstallRequest
import idb.Payload
import idb.TargetDescription
import idb.TargetDescriptionRequest
import idb.XctestListTestsRequest
import idb.XctestRunRequest
import idb.XctestRunResponse
import io.grpc.ManagedChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.util.concurrent.TimeUnit

class IdbClient(
    private val channel: ManagedChannel,
    private val stub: CompanionServiceGrpcKt.CompanionServiceCoroutineStub
) {
    val logger = MarathonLogging.logger("IdbClient")


    suspend fun install(file: File, destination: InstallRequest.Destination = InstallRequest.Destination.APP) {
        logger.info { "Install: $file" }
        val result = stub.install(flow<InstallRequest> {
            logger.info("install start")
            val initRequest = InstallRequest.newBuilder()
                .setDestination(destination)
                .build()
            logger.info("prepare initial request")
            emit(initRequest)
            logger.info("initial request sent")

            val bytesFlow = FileChunkGenerator().generateChunks(file).map {
                ByteString.copyFrom(it)
            }.map {
                Payload.newBuilder().setData(it).build()
            }.map {
                InstallRequest.newBuilder().setPayload(it).build()
            }
            this.emitAll(bytesFlow)
        })
        result.onEach {
            logger.info { "Installed: ${it.progress}" }
        }.collect()
    }

    suspend fun installXCTest(file: File) {
        install(file, InstallRequest.Destination.XCTEST)
    }

    suspend fun describe(): TargetDescription {
        val request = TargetDescriptionRequest.newBuilder().build()
        return stub.describe(request).targetDescription
    }

    suspend fun extractTests(): List<String> {
        val request = XctestListTestsRequest.newBuilder()
            .setAppPath("")
            .setBundleName("")
            .build()
        return stub.xctestListTests(request).namesList
    }

    @kotlinx.coroutines.InternalCoroutinesApi
    suspend fun runTests(tests: List<Test>): Flow<XctestRunResponse> {
        val xcodeTests = tests.map {
            "${it.pkg}/${it.clazz}/${it.method}"
        }
        val request = XctestRunRequest.newBuilder()
            .addAllTestsToRun(xcodeTests)
            .build()
        return stub.xctestRun(request);
    }

    fun dispose() {
        channel.shutdown()
        channel.awaitTermination(1000, TimeUnit.MILLISECONDS)
    }
}
