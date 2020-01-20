package com.malinskiy.marathon.android

import com.malinskiy.marathon.cache.test.key.ComponentCacheKeyProvider
import com.malinskiy.marathon.execution.ComponentInfo
import com.malinskiy.marathon.io.FileHasher

class AndroidComponentCacheKeyProvider(private val fileHasher: FileHasher) : ComponentCacheKeyProvider {

    override suspend fun getCacheKey(componentInfo: ComponentInfo): String {
        componentInfo as AndroidComponentInfo

        val mainApkHash = fileHasher.getHash(componentInfo.testApplicationOutput)
        val testApkHash = componentInfo.applicationOutput?.let { fileHasher.getHash(it) } ?: ""

        return mainApkHash + testApkHash
    }
}
