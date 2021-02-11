package com.malinskiy.marathon.android.executor.listeners.screenshot.gif

import io.ktor.utils.io.*
import kotlin.math.max

/**
 * LZW implementation with clear codes and dynamic table support
 * See specification at https://www.w3.org/Graphics/GIF/spec-gif89a.txt
 */
class LZWEncoder(
    colorDepth: Int
) {
    /**
     * black & white images which have one color bit must be indicated as having a code size of 2
     */
    private val initCodeSize: Int = max(2, colorDepth)

    private val clearCode = 1 shl colorDepth
    private var endOfInformationCode = 0
    private var currentCompressionCodeValue = 0


    var numberOfBits = 0  // number of bits/code
    var maxCode = 0 // maximum code, given n_bits

    var values = IntArray(TABLE_SIZE)
    var codes = IntArray(TABLE_SIZE)

    var clearDict = false
    var minNumberOfBits = 0
    var currentAccumulator = 0
    var currentBitsInAccumulator = 0

    // Number of characters so far in this 'packet'
    var packetSize = 0

    // Define the storage for the packet accumulator
    var packet = ByteArray(MAX_PACKET_SIZE)

    suspend fun outputByte(byte: Byte, outs: ByteWriteChannel) {
        packet[packetSize++] = byte
        if (packetSize == MAX_PACKET_SIZE) {
            tryFlush(outs)
        }
    }

    // Clear out the hash table
    // table clear for block compress
    suspend fun clear(channel: ByteWriteChannel) {
        resetCodeTable()
        currentCompressionCodeValue = clearCode + 2
        clearDict = true

        output(clearCode, channel)
    }

    private fun resetCodeTable() = values.fill(-1, 0, TABLE_SIZE)

    /**
     * Implementation of D. Knuth's algorithm D (vol. 3, sec. 6.4) with G. Knott's dependant hash
     */
    suspend fun compress(sequence: Sequence<Int>, initialBitDepth: Int, outs: ByteWriteChannel) {
        minNumberOfBits = initialBitDepth

        clearDict = false
        numberOfBits = initialBitDepth
        maxCode = maxCodeFor(numberOfBits)
        endOfInformationCode = clearCode + 1
        currentCompressionCodeValue = clearCode + 2
        packetSize = 0
        currentBitsInAccumulator = 0

        val iterator = sequence.iterator()
        var inputAccumulator: Int = iterator.next()

        var hashCodeShift = 0
        var fcode = TABLE_SIZE
        while (fcode < 65536) {
            ++hashCodeShift
            fcode *= 2
        }
        resetCodeTable()
        output(clearCode, outs)
        outerloop@ while (iterator.hasNext()) {
            val inputValue = iterator.next()
            val currentValue = (inputValue shl MAX_CODE_BITS) + inputAccumulator
            val hash1 = primaryHash(inputValue, TABLE_SIZE)
            var idx = hash1
            if (values[idx] == currentValue) {
                inputAccumulator = codes[idx]
                continue
            }

            if (values[idx] >= 0) {
                val hash2 = secondaryHash(currentValue, TABLE_SIZE)
                do {
                    idx -= hash2
                    if (idx < 0) {
                        idx += TABLE_SIZE
                    }
                    if (values[idx] == currentValue) {
                        inputAccumulator = codes[idx]
                        continue@outerloop
                    }
                } while (values[idx] >= 0)
            }
            output(inputAccumulator, outs)
            inputAccumulator = inputValue
            if (currentCompressionCodeValue < MAX_CODE_VALUE) {
                codes[idx] = currentCompressionCodeValue++ // code -> hashtable
                values[idx] = currentValue
            } else {
                clear(outs)
            }
        }
        output(inputAccumulator, outs)
        output(endOfInformationCode, outs)
    }

    private fun primaryHash(value: Int, M: Int): Int {
        return value.rem(M)
    }

    private fun secondaryHash(value: Int, M: Int): Int {
        return 1 + (value.rem(M - 2))
    }

    suspend fun encode(sequence: Sequence<Int>, channel: ByteWriteChannel) {
        channel.writeByte(initCodeSize.toByte())
        compress(sequence, initCodeSize + 1, channel)
        channel.writeByte(BLOCK_TERMINATOR)
    }

    suspend fun tryFlush(outs: ByteWriteChannel) {
        if (packetSize > 0) {
            outs.writeByte(packetSize.toByte())
            outs.writeFully(packet, 0, packetSize)
            packetSize = 0
        }
    }

    suspend fun output(code: Int, outs: ByteWriteChannel) {
        currentAccumulator = currentAccumulator and masks[currentBitsInAccumulator]
        currentAccumulator = if (currentBitsInAccumulator > 0) {
            currentAccumulator or (code shl currentBitsInAccumulator)
        } else {
            code
        }
        currentBitsInAccumulator += numberOfBits
        while (currentBitsInAccumulator >= 8) {
            flushByteFromAccumulator(outs)
        }

        // If the next entry is going to be too big for the code size,
        // then increase it, if possible.
        if (currentCompressionCodeValue > maxCode || clearDict) {
            if (clearDict) {
                numberOfBits = minNumberOfBits
                maxCode = maxCodeFor(numberOfBits)
                clearDict = false
            } else {
                ++numberOfBits
                maxCode = maxCodeFor(numberOfBits)
            }
        }
        if (code == endOfInformationCode) {
            while (currentBitsInAccumulator > 0) {
                flushByteFromAccumulator(outs)
            }
            tryFlush(outs)
        }
    }

    private suspend fun flushByteFromAccumulator(channel: ByteWriteChannel) {
        outputByte((currentAccumulator and 0xff).toByte(), channel)
        currentAccumulator = currentAccumulator shr 8
        currentBitsInAccumulator -= 8
    }

    companion object {
        const val MAX_CODE_BITS = 12
        const val MAX_CODE_VALUE = 0xFFF

        const val MAX_PACKET_SIZE = 255

        //twin prime with 5009, important!
        const val TABLE_SIZE = 5011
        const val BLOCK_TERMINATOR = 0
        val masks = intArrayOf(
            0x0000, 0x0001, 0x0003, 0x0007,
            0x000F, 0x001F, 0x003F, 0x007F,
            0x00FF, 0x01FF, 0x03FF, 0x07FF,
            0x0FFF, 0x1FFF, 0x3FFF, 0x7FFF,
            0xFFFF
        )

        fun maxCodeFor(numberOfBits: Int) = when (numberOfBits) {
            1 -> 1
            2 -> 3
            3 -> 7
            4 -> 15
            5 -> 31
            6 -> 63
            7 -> 127
            8 -> 255
            9 -> 511
            10 -> 1023
            11 -> 2047
            12 -> 4095
            else -> {
                throw RuntimeException("Unsupported LZW code length $numberOfBits")
            }
        }
    }
}
