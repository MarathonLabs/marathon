package com.malinskiy.marathon.ios.idb

import idb.CompanionServiceGrpcKt
import idb.TargetDescription
import idb.TargetDescriptionRequest
import idb.XctestRunRequest
import idb.XctestRunResponse
import io.grpc.ManagedChannel
import kotlinx.coroutines.flow.FlowCollector
import java.util.concurrent.TimeUnit

class IdbClient(
    private val channel: ManagedChannel,
    private val stub: CompanionServiceGrpcKt.CompanionServiceCoroutineStub
) {
    suspend fun install() {
        stub
    }

    suspend fun installXCTest() {

    }

    suspend fun describe(): TargetDescription {
        return stub.describe(TargetDescriptionRequest.newBuilder().build()).targetDescription
    }

    @kotlinx.coroutines.InternalCoroutinesApi
    suspend fun runTests() {
        val response = stub.xctestRun(XctestRunRequest.newBuilder().build());
        response.collect(object: FlowCollector<XctestRunResponse>{
            override suspend fun emit(value: XctestRunResponse) {
                TODO("Not yet implemented")
            }
        })
    }

    fun dispose(){
        channel.shutdown()
        channel.awaitTermination(1000, TimeUnit.MILLISECONDS)
    }
}
