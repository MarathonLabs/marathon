package com.malinskiy.marathon.cache.gradle

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.time.Duration

class GradleCacheContainer constructor(image: String = "$DEFAULT_IMAGE_NAME:$DEFAULT_TAG") :
    GenericContainer<GradleCacheContainer>(image) {

    init {
        addExposedPorts(DEFAULT_PORT)
        waitStrategy = Wait
            .forLogMessage(".*Build cache node started(?s).*", 1)
            .withStartupTimeout(Duration.ofSeconds(60))
    }

    val cacheUrl: String
        get() = "http://$containerIpAddress:$httpPort/cache/"

    private val httpPort: Int
        get() = getMappedPort(DEFAULT_PORT)

    private companion object {
        private const val DEFAULT_IMAGE_NAME = "gradle/build-cache-node"
        private const val DEFAULT_TAG = "9.0"
        private const val DEFAULT_PORT = 5071
    }
}
