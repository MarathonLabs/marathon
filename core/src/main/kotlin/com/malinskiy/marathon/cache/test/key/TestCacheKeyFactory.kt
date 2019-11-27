package com.malinskiy.marathon.cache.test.key

import com.malinskiy.marathon.cache.CacheKey
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.test.Test
import java.io.OutputStream
import java.io.Writer
import java.math.BigInteger
import java.security.DigestOutputStream
import java.security.MessageDigest

class TestCacheKeyFactory(
    private val componentCacheKeyProvider: ComponentCacheKeyProvider,
    private val versionNameProvider: VersionNameProvider,
    private val configuration: Configuration
) {

    suspend fun getCacheKey(poolId: DevicePoolId, test: Test): CacheKey {
        val digestInputStream = createDigestOutputStream()
        val componentCachingKey = componentCacheKeyProvider.getCacheKey(test.componentInfo)

        digestInputStream
            .bufferedWriter()
            .use {
                it.write(versionNameProvider.versionName)
                it.writeConfiguration(configuration)
                it.write(componentCachingKey)
                it.write(poolId.name)
                it.write(test.pkg)
                it.write(test.clazz)
                it.write(test.method)
            }

        val key = digestInputStream
            .messageDigest
            .digest()
            .let { BigInteger(1, it).toString(16) }
            .toString()

        return TestCacheKey(key, test)
    }

    /**
     * Write parameters that may affect test execution result or collected artifacts
     */
    private fun Writer.writeConfiguration(configuration: Configuration) {
        write("codeCoverageEnabled=${configuration.isCodeCoverageEnabled}")
    }

    private fun createDigestOutputStream(): DigestOutputStream =
        DigestOutputStream(
            object : OutputStream() {
                override fun write(b: Int) {}
            },
            MessageDigest.getInstance("MD5")
        )
}
