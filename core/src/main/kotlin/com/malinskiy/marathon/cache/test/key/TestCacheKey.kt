package com.malinskiy.marathon.cache.test.key

import com.malinskiy.marathon.cache.CacheKey
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName

class TestCacheKey(
    override val key: String,
    test: Test
) : CacheKey {

    private val description = "CacheKey{test=Test(${test.toTestName()}, key=$key)}"

    override fun toString(): String = description

    override fun equals(other: Any?): Boolean {
        val otherKey = other as? TestCacheKey ?: return false
        return otherKey.key == key
    }

    override fun hashCode(): Int = key.hashCode()

}
