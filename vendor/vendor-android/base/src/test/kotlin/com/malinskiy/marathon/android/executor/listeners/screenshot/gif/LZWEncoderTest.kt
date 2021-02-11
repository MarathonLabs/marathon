package com.malinskiy.marathon.android.executor.listeners.screenshot.gif

import com.malinskiy.marathon.android.extension.writeAsynchronously
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File

class LZWEncoderTest {

    @ParameterizedTest
    @CsvSource(
        value = [
            "lzw/sample_1.bin:lzw/sample_1.lzw",
            "lzw/sample_2.bin:lzw/sample_2.lzw",
        ],
        delimiter = ':'
    )
    fun testOnFixtures(input: String, output: String) {
        val encoder = LZWEncoder(colorDepth = 8)

        runBlocking {
            val frameFile = File(javaClass.classLoader.getResource(input).file)
            val tempFile = createTempFile()

            tempFile.writeAsynchronously(Dispatchers.IO) { channel: ByteWriteChannel ->
                encoder.encode(frameFile.readBytes().asSequence().map { it.toUByte().toInt() }, channel)
            }

            val expectedFile = File(javaClass.classLoader.getResource(output).file)

            val expected: ByteArray = expectedFile.readBytes()
            val actual = tempFile.readBytes()

            assertThat(actual, equalTo(expected))
        }
    }
}
