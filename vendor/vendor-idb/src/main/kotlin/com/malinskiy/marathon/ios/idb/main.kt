package com.malinskiy.marathon.ios.idb

import idb.CompanionServiceGrpc
import idb.CompanionServiceGrpcKt
import idb.ListAppsRequest
import idb.TargetDescriptionRequest
import io.grpc.CallOptions
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

fun main() {
    runBlocking {
        val channel = ManagedChannelBuilder.forAddress("192.168.2.163", 10882).usePlaintext().build()
        val client = CompanionServiceGrpcKt.CompanionServiceCoroutineStub(channel, CallOptions.DEFAULT.withWaitForReady())
        val targets = client.listApps(ListAppsRequest.newBuilder().build())
        val describe = client.describe(TargetDescriptionRequest.newBuilder().build())
        val s = CompanionServiceGrpc.newStub(channel)
        val b = CompanionServiceGrpc.newBlockingStub(channel)
        val f = CompanionServiceGrpc.newFutureStub(channel)

        channel.shutdown()
        channel.awaitTermination(1000, TimeUnit.SECONDS)
    }

}
