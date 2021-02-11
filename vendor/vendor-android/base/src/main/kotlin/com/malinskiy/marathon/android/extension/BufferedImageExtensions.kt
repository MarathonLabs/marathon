package com.malinskiy.marathon.android.extension

import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte

/**
 * If the buffered image is not of type, convert to it and return the new underlying byte array
 */
fun BufferedImage.convert(type: Int = BufferedImage.TYPE_3BYTE_BGR): ByteArray {
    val imageInput: BufferedImage = if (this.type != type) {
        val temp = BufferedImage(width, height, type)
        val g = temp.createGraphics()
        g.drawImage(this, 0, 0, null)
        temp
    } else {
        this
    }
    return (imageInput.raster.dataBuffer as DataBufferByte).data
}
