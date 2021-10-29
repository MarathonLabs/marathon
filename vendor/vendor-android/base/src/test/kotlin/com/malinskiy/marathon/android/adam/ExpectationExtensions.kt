package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.request.Feature
import com.malinskiy.adam.server.stub.dsl.Expectation

fun Expectation.features(serialNo: String, supportedFeatures: List<Feature> = listOf(Feature.CMD)) {
    other("host-serial:$serialNo:features") {
        output.respondOkay()
        output.respondStringV1(supportedFeatures.joinToString(separator = ","))
    }
}
