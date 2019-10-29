package com.malinskiy.marathon.ios.cmd.remote

import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object SshjCommandOutputLineBufferSpek : Spek(
    {
        abstract class LineCollector {
            abstract val lines: Collection<String>
            abstract fun onLine(line: String)
        }

        describe("SshjCommandOutputLineBuffer") {
            val lines by memoized { arrayListOf<String>() }
            val buffer by memoized {
                SshjCommandOutputLineBuffer() { lines.add(it) }
            }

            on("flushing empty buffer") {
                it("should not produce any lines") {
                    buffer.flush()

                    lines.shouldBeEmpty()
                }
            }

            on("appending data multiple times") {
                val incomingText1 = "first one\nfirst two\nfirst three\n"
                    .also { buffer.append(it.toByteArray()) }
                val incomingText2 = "second one\nsecond two\nsecond three\n"

                it("should not remove previously received lines") {
                    buffer.append(incomingText2.toByteArray())

                    buffer.flush()

                    lines.count() shouldEqual (incomingText1 + incomingText2).lineCount()
                }
            }

            on("flushing incomplete data") {
                val incomingText = "first one\nfirst two\nfirst three"
                    .also { buffer.append(it.toByteArray()) }

                it("should produce only terminated lines") {
                    buffer.flush()

                    lines.count() shouldEqual incomingText.lineCount()
                }
            }

            on("draining incomplete data") {
                val incomingText = "first one\nfirst two\nfirst three"
                    .also { buffer.append(it.toByteArray()) }

                it("should produce both termninated and unterminated lines") {
                    buffer.drain()

                    lines.count() shouldEqual incomingText.lineCount() + 1
                }
            }

            on("closing buffer") {
                val incomingText = "first one\nfirst two\nfirst three"
                    .also { buffer.append(it.toByteArray()) }

                it("should drain all data") {
                    buffer.close()

                    lines.count() shouldEqual incomingText.lineCount() + 1
                }
            }
        }
    })

private fun String.lineCount(): Int = count { it == '\n' }
