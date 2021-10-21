package com.malinskiy.marathon.ios.cmd.remote

import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class SshjCommandOutputLineBufferTest {
    private val lines = arrayListOf<String>()
    private val buffer = SshjCommandOutputLineBuffer { lines.add(it) }

    @Test
    fun `flushing empty buffer should not produce any lines`() {
        buffer.flush()

        lines.shouldBeEmpty()
    }

    @Test
    fun `appending data multiple times should not remove previously received lines`() {
        val incomingText1 = "first one\nfirst two\nfirst three\n"
            .also { buffer.append(it.toByteArray()) }
        val incomingText2 = "second one\nsecond two\nsecond three\n"
        buffer.append(incomingText2.toByteArray())

        buffer.flush()

        lines.count() shouldBeEqualTo (incomingText1 + incomingText2).lineCount()
    }

    @Test
    fun `flushing incomplete data should produce only terminated lines`() {
        val incomingText = "first one\nfirst two\nfirst three"
            .also { buffer.append(it.toByteArray()) }
        buffer.flush()

        lines.count() shouldBeEqualTo incomingText.lineCount()
    }

    @Test
    fun `draining incomplete data should produce both terminated and unterminated lines`() {
        val incomingText = "first one\nfirst two\nfirst three"
            .also { buffer.append(it.toByteArray()) }
        buffer.drain()

        lines.count() shouldBeEqualTo incomingText.lineCount() + 1
    }

    @Test
    fun `closing buffer should drain all data`() {
        val incomingText = "first one\nfirst two\nfirst three"
            .also { buffer.append(it.toByteArray()) }
        buffer.close()

        lines.count() shouldBeEqualTo incomingText.lineCount() + 1
    }
}

private fun String.lineCount(): Int = count { it == '\n' }
