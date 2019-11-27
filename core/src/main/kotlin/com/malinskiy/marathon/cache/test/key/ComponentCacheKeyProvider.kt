package com.malinskiy.marathon.cache.test.key

import com.malinskiy.marathon.execution.ComponentInfo

interface ComponentCacheKeyProvider {
    suspend fun getCacheKey(componentInfo: ComponentInfo): String
}
