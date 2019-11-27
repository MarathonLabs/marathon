package com.malinskiy.marathon.ios

import com.malinskiy.marathon.cache.test.key.ComponentCacheKeyProvider
import com.malinskiy.marathon.execution.ComponentInfo

class IOSComponentCacheKeyProvider : ComponentCacheKeyProvider {

    override suspend fun getCacheKey(componentInfo: ComponentInfo): String {
        TODO()
    }
}
