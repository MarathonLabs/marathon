package com.malinskiy.marathon.cache.gradle

import com.malinskiy.marathon.cache.SimpleCacheKey
import com.malinskiy.marathon.cache.SimpleEntryReader
import com.malinskiy.marathon.cache.SimpleEntryWriter
import com.malinskiy.marathon.cache.config.RemoteCacheConfiguration
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.lifecycle.CachingMode

class GradleHttpCacheServiceSpek : Spek(
    {

        val container = GradleCacheContainer()

        val cacheService = memoized(mode = CachingMode.TEST) {
            GradleHttpCacheService(RemoteCacheConfiguration.Enabled(container.cacheUrl))
        }

        afterEachTest {
            cacheService().close()
        }

        beforeGroup {
            container.start()
        }

        afterGroup {
            container.stop()
        }

        describe("GradleHttpCacheService") {
            group("basics") {
                it("load with empty cache - should return false") {
                    runBlocking {
                        val reader = SimpleEntryReader()
                        val result = cacheService().load(SimpleCacheKey("this_key_does_not_exists"), reader)

                        result shouldEqual false
                        reader.readInvoked shouldEqual false
                    }
                }

                it("save to cache and load - should return the same data") {
                    runBlocking {
                        cacheService().store(SimpleCacheKey("test"), SimpleEntryWriter("qwerty"))
                        val reader = SimpleEntryReader()
                        val result = cacheService().load(SimpleCacheKey("test"), reader)

                        result shouldEqual true
                        reader.readInvoked shouldEqual true
                        reader.data shouldEqual "qwerty"
                    }
                }
            }
        }
    })
