package com.malinskiy.marathon.sample.gradlejunit4

import org.junit.Test

class HelloWorldGeneratorTest {
    @Test
    fun testHelloWorld() {
        assert(HelloWorldGenerator().printHelloWorld() == "Hello world!")
    }
}
