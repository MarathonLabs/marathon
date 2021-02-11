package com.malinskiy.marathon.android.executor.listeners.screenshot.gif

import io.ktor.util.cio.*
import io.ktor.utils.io.*
import java.awt.Color
import java.io.IOException
import kotlin.math.roundToInt

/**
 *
 * No copyright asserted on the source code of this class. May be used for any
 * purpose, however, refer to the Unisys LZW patent for restrictions on use of
 * the associated LZWEncoder class. Please forward any corrections to
 * kweiner@fmsware.com.
 *
 * @author Kevin Weiner, FM Software
 * @version 1.03 November 2003
 * ported to Kotlin by Anton Malinskiy
 *
 * @param repeat Sets the number of times the set of GIF frames should be played. Default is 1. 0 means play indefinitely
 * @param width resulting width
 * @param height resulting height
 * @param transparent Sets the transparent color for the last added frame and any subsequent frames. Since all colors are subject to modification in the quantization process, the color in the final palette for each frame closest to the given color becomes the transparent color for that frame. May be set to null to indicate no transparent color.
 * @param transparentIndex transparent index in color table
 *
 */
class GifEncoderKotlin(
    val repeat: Short = 0,
    private var width: Short = 0,
    private var height: Short = 0,
    var transparent: Color? = null,
    var transparentIndex: Int = 0
) {
    /**
     * frame delay (hundredths)
     */
    var delay: Short = 0
        /**
         * Sets the delay time between each frame, or changes it for subsequent frames
         * (applies to last frame added).
         *
         * @param ms delay time in milliseconds
         */
        set(ms) {
            field = (ms / 10.0f).roundToInt().toShort()
        }

    /**
     * Ready to output frames
     */
    private var started = false
    private var pixels: ByteArray? = null // BGR byte array from frame
    private var indexedPixels: ByteArray? = null // converted frame indexed to palette
    private var colorTab: ByteArray? = null // RGB palette
    private var usedEntry = BooleanArray(256) // active palette entries
    private var palSize = 7 // color table size (bits-1)

    /**
     * Sets the GIF frame disposal code for the last added frame and any
     * subsequent frames. Default is 0 if no transparent color has been set,
     * otherwise 2.
     *
     * @param code int disposal code.
     */
    private var dispose = -1 // disposal code (-1 = use default)
    private var firstFrame = true
    private var sample = 10 // default sample interval for quantizer

    /**
     * Adds next GIF frame. The frame is not written immediately, but is actually
     * deferred until the next frame is received so that timing data can be
     * inserted.
     *
     * @param byteArray BGR formatted image buffer
     * @return true if successful.
     */
    suspend fun addFrame(byteArray: ByteArray, width: Short, height: Short, channel: ByteWriteChannel): Boolean {
        if (!started) {
            return false
        }
        var ok = true
        try {
            pixels = byteArray
            this.width = width
            this.height = height
            if (byteArray.size != width * height * 3) {
                throw RuntimeException("Invalid input. $width x $height x 3 != ${byteArray.size}")
            }

            analyzePixels() // build color table & map pixels
            if (firstFrame) {
                writeLSD(channel) // logical screen descriptor
                writePalette(channel) // global color table
                if (repeat >= 0) {
                    // use NS app extension to indicate reps
                    writeNetscapeExt(channel)
                }
            }
            writeGraphicCtrlExt(channel) // write graphic control extension
            writeImageDesc(channel) // image descriptor
            if (!firstFrame) {
                writePalette(channel) // local color table
            }
            writePixels(channel) // encode and write pixel data
            firstFrame = false
        } catch (e: IOException) {
            ok = false
        }
        return ok
    }

    suspend fun finish(channel: ByteWriteChannel): Boolean {
        if (!started) return false
        var ok = true
        started = false
        channel.writeByte(0x3b) // gif trailer

        // reset for subsequent use
        transparentIndex = 0
        pixels = null
        indexedPixels = null
        colorTab = null
        firstFrame = true
        return ok
    }

    /**
     * Sets frame rate in frames per second. Equivalent to
     * `setDelay(1000/fps)`.
     *
     * @param fps
     * float frame rate (frames per second)
     */
    fun setFrameRate(fps: Float) {
        if (fps != 0f) {
            delay = (100f / fps).roundToInt().toShort()
        }
    }

    /**
     * Sets quality of color quantization (conversion of images to the maximum 256
     * colors allowed by the GIF specification). Lower values (minimum = 1)
     * produce better colors, but slow processing significantly. 10 is the
     * default, and produces good color mapping at reasonable speeds. Values
     * greater than 20 do not yield significant improvements in speed.
     *
     * @param quality int greater than 0.
     * @return
     */
    fun setQuality(quality: Int) {
        var quality = quality
        if (quality < 1) quality = 1
        sample = quality
    }

    /**
     * Initiates GIF file creation on the given stream. The stream is not closed
     * automatically.
     *
     * @param channel
     * OutputStream on which GIF images are written.
     * @return false if initial write failed.
     */
    suspend fun start(channel: ByteWriteChannel): Boolean {
        var ok = true
        channel.writeString("GIF89a") // header
        return ok.also { started = it }
    }

    /**
     * Analyzes image colors and creates color map.
     */
    protected fun analyzePixels() {
        val len = pixels!!.size
        val nPix = len / 3
        indexedPixels = ByteArray(nPix)
        val nq = NeuQuant(sample)
        // initialize quantizer
        colorTab = nq.process(pixels!!) // create reduced palette
        // convert map from BGR to RGB
        var i = 0
        while (i < colorTab!!.size) {
            val temp = colorTab!![i]
            colorTab!![i] = colorTab!![i + 2]
            colorTab!![i + 2] = temp
            usedEntry[i / 3] = false
            i += 3
        }
        // map image pixels to new palette
        var k = 0
        for (i in 0 until nPix) {
            val index = nq.map(pixels!![k++].toUByte(), pixels!![k++].toUByte(), pixels!![k++].toUByte())
            usedEntry[index] = true
            indexedPixels!![i] = index.toByte()
        }
        pixels = null
        palSize = 7
        // get closest match to transparent color if specified
        if (transparent != null) {
            transparentIndex = findClosest(transparent!!)
        }
    }

    /**
     * Returns index of palette color closest to c
     *
     */
    private fun findClosest(c: Color): Int {
        if (colorTab == null) return -1
        val r = c.red
        val g = c.green
        val b = c.blue
        var minpos = 0
        var dmin = 256 * 256 * 256
        val len = colorTab!!.size
        var i = 0
        while (i < len) {
            val dr: Int = r - (colorTab!![i++])
            val dg: Int = g - (colorTab!![i++])
            val db: Int = b - (colorTab!![i])
            val d = dr * dr + dg * dg + db * db
            val index = i / 3
            if (usedEntry[index] && d < dmin) {
                dmin = d
                minpos = index
            }
            i++
        }
        return minpos
    }// create new image with right size/format

    /**
     * Writes Graphic Control Extension
     */
    private suspend fun writeGraphicCtrlExt(channel: ByteWriteChannel) {
        channel.writeByte(0x21) // extension introducer
        channel.writeByte(0xf9) // GCE label
        channel.writeByte(4) // data block size
        val transp: Int
        var disp: Int
        if (transparent == null) {
            transp = 0
            disp = 0 // dispose = no action
        } else {
            transp = 1
            disp = 2 // force clear if using transparent color
        }
        if (dispose >= 0) {
            disp = dispose and 7 // user override
        }
        disp = disp shl 2

        // packed fields
        channel.writeByte(
            0 or  // 1:3 reserved
                disp or  // 4:6 disposal
                0 or  // 7 user input - 0 = none
                transp
        ) // 8 transparency flag
        channel.writeShortLittleEndian(delay) // delay x 1/100 sec
        channel.writeByte(transparentIndex) // transparent color index
        channel.writeByte(0) // block terminator
    }

    /**
     * Writes Image Descriptor
     */
    private suspend fun writeImageDesc(channel: ByteWriteChannel) {
        channel.writeByte(0x2c) // image separator
        channel.writeShortLittleEndian(0) // image position x,y = 0,0
        channel.writeShortLittleEndian(0)
        channel.writeShortLittleEndian(width) // image size
        channel.writeShortLittleEndian(height)
        // packed fields
        if (firstFrame) {
            // no LCT - GCT is used for first (or only) frame
            channel.writeByte(0)
        } else {
            // specify normal LCT
            channel.writeByte(
                0x80 or  // 1 local color table 1=yes
                    0 or  // 2 interlace - 0=no
                    0 or  // 3 sorted - 0=no
                    0 or  // 4-5 reserved
                    palSize
            ) // 6-8 size of color table
        }
    }

    /**
     * Writes Logical Screen Descriptor
     */
    private suspend fun writeLSD(channel: ByteWriteChannel) {
        // logical screen size
        channel.writeShortLittleEndian(width)
        channel.writeShortLittleEndian(height)
        // packed fields
        channel.writeByte(
            0x80 or  // 1 : global color table flag = 1 (gct used)
                0x70 or  // 2-4 : color resolution = 7
                0x00 or  // 5 : gct sort flag = 0
                palSize
        ) // 6-8 : gct size
        channel.writeByte(0) // background color index
        channel.writeByte(0) // pixel aspect ratio - assume 1:1
    }

    /**
     * Writes Netscape application extension to define repeat count.
     */
    private suspend fun writeNetscapeExt(channel: ByteWriteChannel) {
        channel.writeByte(0x21) // extension introducer
        channel.writeByte(0xff) // app extension label
        channel.writeByte(11) // block size
        channel.writeString("NETSCAPE" + "2.0") // app id + auth code
        channel.writeByte(3) // sub-block size
        channel.writeByte(1) // loop sub-block id
        channel.writeShortLittleEndian(repeat) // loop count (extra iterations, 0=repeat forever)
        channel.writeByte(0) // block terminator
    }

    /**
     * Writes color table
     */
    private suspend fun writePalette(channel: ByteWriteChannel) {
        channel.writeFully(colorTab!!, 0, colorTab!!.size)
        val n = 3 * 256 - colorTab!!.size
        for (i in 0 until n) {
            channel.writeByte(0)
        }
    }

    /**
     * Encodes and writes pixel data
     */
    private suspend fun writePixels(channel: ByteWriteChannel) {
        val encoder = LZWEncoder(colorDepth)
        encoder.encode(indexedPixels!!.asSequence().map { it.toInt() and 0xFF }, channel)
    }

    private suspend fun ByteWriteChannel.writeString(s: String) {
        writeFully(s.toByteArray(Charsets.US_ASCII))
    }

    companion object {
        /**
         * number of bit planes
         */
        const val colorDepth = 8
    }
}
