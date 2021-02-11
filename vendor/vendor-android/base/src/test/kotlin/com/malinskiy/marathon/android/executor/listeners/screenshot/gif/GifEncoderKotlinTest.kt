package com.malinskiy.marathon.android.executor.listeners.screenshot.gif

import com.github.romankh3.image.comparison.ImageComparison
import com.github.romankh3.image.comparison.ImageComparisonUtil
import com.github.romankh3.image.comparison.model.ImageComparisonResult
import com.malinskiy.marathon.android.extension.convert
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class GifEncoderKotlinTest {
    @Test
    fun testSample1() = runBlocking {
        val file = createTempFile()
        var writer: GifEncoderKotlin? = null
        var writeChannel: ByteWriteChannel? = null
        try {
            writeChannel = file.writeChannel()
            writer = GifEncoderKotlin(repeat = 0)
            writer.start(writeChannel)
            writer.delay = 100
            for (i in 1..3) {
                val frameFile = File(javaClass.classLoader.getResource("gif/input_1/${i}.png").file)
                val frame = ImageIO.read(frameFile)
                writer.addFrame(
                    frame.convert(BufferedImage.TYPE_3BYTE_BGR),
                    frame.width.toShort(),
                    frame.height.toShort(),
                    writeChannel
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            withContext(NonCancellable) {
                writer?.finish(writeChannel!!)
            }
            writeChannel?.close()
        }

        val reader = ImageIO.getImageReadersByFormatName("gif").next()
        reader.input = ImageIO.createImageInputStream(file)
        assertThat(reader.getNumImages(true), `is`(3))

        for (i in 0..2) {
            val actualFrame = reader.read(i)
            val expectedFrame = ImageIO.read(File(javaClass.classLoader.getResource("gif/input_1/${i + 1}.png").file))
            val result = compare(expectedFrame, actualFrame)
            assertThat("Difference should be less than 1 percent", result.differencePercent < 1.0)
        }
    }

    private fun compare(
        expected: BufferedImage,
        actual: BufferedImage
    ): ImageComparisonResult {
        val imageComparison = ImageComparison(expected, actual)
        val comparisonResult = imageComparison.compareImages()
        val comparisonImage = createTempFile(suffix = ".png")
        ImageComparisonUtil.saveImage(comparisonImage, comparisonResult.result)
        return comparisonResult
    }
}
