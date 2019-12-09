package com.malinskiy.marathon.test

import com.malinskiy.marathon.cache.test.key.ComponentCacheKeyProvider
import com.malinskiy.marathon.execution.ComponentInfo

class StubComponentCacheKeyProvider : ComponentCacheKeyProvider {

    override suspend fun getCacheKey(componentInfo: ComponentInfo): String = componentInfo.name

}
