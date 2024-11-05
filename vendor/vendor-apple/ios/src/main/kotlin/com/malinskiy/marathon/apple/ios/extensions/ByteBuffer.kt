package com.malinskiy.marathon.apple.ios.extensions

import java.nio.Buffer
import java.nio.ByteBuffer

/**
 * Mitigation of running JDK 9 code on JRE 8
 *
 * java.lang.NoSuchMethodError: java.nio.ByteBuffer.xxx()Ljava/nio/ByteBuffer;
 */
fun ByteBuffer.compatRewind() = ((this as Buffer).rewind() as ByteBuffer)
fun ByteBuffer.compatLimit(newLimit: Int) = ((this as Buffer).limit(newLimit) as ByteBuffer)
fun ByteBuffer.compatPosition(newLimit: Int) = ((this as Buffer).position(newLimit) as ByteBuffer)
fun ByteBuffer.compatFlip() = ((this as Buffer).flip() as ByteBuffer)
fun ByteBuffer.compatClear() = ((this as Buffer).clear() as ByteBuffer)
