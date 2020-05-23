package com.malinskiy.marathon.ios.idb.grpc

import com.google.protobuf.ByteString
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
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.concurrent.TimeUnit

class IdbClient(
    private val channel: ManagedChannel,
    private val stub: CompanionServiceGrpcKt.CompanionServiceCoroutineStub
) {
    suspend fun install(file: File) {

        val result = stub.install(flow {

        })
    }

    suspend fun installXCTest(file: File) {
        val result = stub.install(flow {

            val payload = Payload.newBuilder().setData(ByteString.EMPTY).build();
            val request = InstallRequest.newBuilder()
                .setDestination(InstallRequest.Destination.XCTEST)
                .setPayload(payload)
                .build()
            emit(request)
        })
    }

    suspend fun describe(): TargetDescription {
        val request = TargetDescriptionRequest.newBuilder().build()
        return stub.describe(request).targetDescription
    }

    suspend fun extractTests() : List<String> {
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

    fun dispose(){
        channel.shutdown()
        channel.awaitTermination(1000, TimeUnit.MILLISECONDS)
    }
}
