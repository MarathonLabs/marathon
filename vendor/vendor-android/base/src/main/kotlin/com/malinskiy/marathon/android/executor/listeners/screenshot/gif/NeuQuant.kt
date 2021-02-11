package com.malinskiy.marathon.android.executor.listeners.screenshot.gif

import java.util.*

/**
 * NeuQuant Neural-Net Quantization Algorithm
 * ------------------------------------------
 *
 * Copyright (c) 1994 Anthony Dekker
 *
 * NEUQUANT Neural-Net quantization algorithm by Anthony Dekker, 1994. See
 * "Kohonen neural networks for optimal colour quantization" in "Network:
 * Computation in Neural Systems" Vol. 5 (1994) pp 351-367. for a discussion of
 * the algorithm.
 *
 * Any party obtaining a copy of these files from the author, directly or
 * indirectly, is granted, free of charge, a full and unrestricted irrevocable,
 * world-wide, paid up, royalty-free, nonexclusive right and license to deal in
 * this software and documentation files (the "Software"), including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons who
 * receive copies from any such party to do so, with the only requirement being
 * that this copyright notice remain intact.
 *
 * Adapted from Jef Poskanzer's Java port by way of J. M. G. Elliott.
 * K Weiner 12/00
 * Ported to Java 12/00 K Weiner
 * Clean up for Kotlin 12/2020 Anton Malinskiy
 *
 * @param samplingFactor sampling factor 1..30, controls the quality 1 being the best and 30 - the worst
 */
internal class NeuQuant(var samplingFactor: Int) {
    /* biased by 10 bits */
    private var alphadec = 0
    private var network: Array<IntArray?>
    private var netindex = IntArray(NET_SIZE)
    private var bias = IntArray(NET_SIZE)
    private var freq = IntArray(NET_SIZE)
    private var radpower = IntArray(INITRAD)

    /**
     * @param lengthcount H*W*3
     */
    fun process(thepicture: ByteArray, lengthcount: Int = thepicture.size): ByteArray {
        learn(thepicture, lengthcount)
        unbiasnet()
        inxbuild()
        return colorMap()
    }

    private fun colorMap(): ByteArray {
        val map = ByteArray(3 * NET_SIZE)
        val index = IntArray(NET_SIZE)
        for (i in 0 until NET_SIZE) {
            index[network[i]!![3]] = i
        }
        var k = 0
        for (i in 0 until NET_SIZE) {
            val j = index[i]
            map[k++] = network[j]!![0].toByte()
            map[k++] = network[j]!![1].toByte()
            map[k++] = network[j]!![2].toByte()
        }
        return map
    }

    /**
     * Insertion sort of network and building of netindex[0..255] (to do after unbias)
     */
    private fun inxbuild() {
        var smallpos: Int
        var smallval: Int
        var q: IntArray?
        var previouscol = 0
        var startpos = 0

        for (i in 0 until NET_SIZE) {
            val p = network[i]
            smallpos = i
            smallval = p!![1] /* index on g */
            /* find smallest in i..netsize-1 */

            for (j in i + 1 until NET_SIZE) {
                q = network[j]
                if (q!![1] < smallval) { /* index on g */
                    smallpos = j
                    smallval = q[1] /* index on g */
                }
            }
            q = network[smallpos]
            /* swap p (i) and q (smallpos) entries */
            if (i != smallpos) {
                for (k in 0..3) {
                    val t = q!![k]
                    q[k] = p[k]
                    p[k] = t
                }
            }
            /* smallval entry is now in position i */
            if (smallval != previouscol) {
                netindex[previouscol] = startpos + i shr 1
                for (j in previouscol + 1 until smallval) {
                    netindex[j] = i
                }
                previouscol = smallval
                startpos = i
            }
        }

        netindex[previouscol] = startpos + MAX_NET_POS shr 1
        for (i in previouscol + 1 until 256) {
            netindex[i] = MAX_NET_POS
        }
    }

    /**
     * Main Learning Loop
     */
    private fun learn(thepicture: ByteArray, lengthcount: Int) {
        var radius = INIT_RADIUS
        var rad = radius shr RADIUS_BIAS_SHIFT
        var alpha = INIT_ALPHA
        val samplepixels = lengthcount / (3 * samplingFactor)
        var delta = samplepixels / NCYCLES
        var pix = 0
        val lim = lengthcount

        if (lengthcount < MIN_PICTURE_BYTES) {
            samplingFactor = 1
        }
        alphadec = 30 + (samplingFactor - 1) / 3
        if (rad <= 1) {
            rad = 0
        }
        for (i in 0 until rad) {
            radpower[i] = alpha * ((rad * rad - i * i) * RADBIAS / (rad * rad))
        }

        //Begin learning
        val step = when {
            lengthcount < MIN_PICTURE_BYTES -> 3
            lengthcount % PRIME1 != 0 -> 3 * PRIME1
            lengthcount % PRIME2 != 0 -> 3 * PRIME2
            lengthcount % PRIME3 != 0 -> 3 * PRIME3
            else -> 3 * PRIME4
        }
        var i = 0
        while (i < samplepixels) {
            val b = thepicture[pix + 0].toUByte().toInt() shl NET_BIAS_SHIFT
            val g = thepicture[pix + 1].toUByte().toInt() shl NET_BIAS_SHIFT
            val r = thepicture[pix + 2].toUByte().toInt() shl NET_BIAS_SHIFT
            val j = contest(b, g, r)
            altersingle(alpha, j, b, g, r)
            if (rad != 0) {
                alterneigh(rad, j, b, g, r) /* alter neighbours */
            }
            pix += step
            if (pix >= lim) {
                pix -= lengthcount
            }
            i++
            if (delta == 0) {
                delta = 1
            }
            if (i % delta == 0) {
                alpha -= alpha / alphadec
                radius -= radius / RADIUS_DECREASE
                rad = radius shr RADIUS_BIAS_SHIFT
                if (rad <= 1) {
                    rad = 0
                }
                for (j in 0 until rad) {
                    radpower[j] = alpha * ((rad * rad - j * j) * RADBIAS / (rad * rad))
                }
            }
        }
        //Finished learning
    }

    /**
     * Search for BGR values 0..255 (after net is unbiased) and return colour index
     */
    fun map(b: UByte, g: UByte, r: UByte): Int {
        var i = netindex[g.toInt()] /* index on g */
        var j = i - 1 /* start at netindex[g] and work outwards */
        var dist: Int
        var a: Int
        var bestd = 1000 /* biggest possible dist is 256*3 */
        var p: IntArray?
        var best = -1

        while (i < NET_SIZE || j >= 0) {
            if (i < NET_SIZE) {
                p = network[i]
                dist = p!![1] - g.toInt() /* inx key */
                if (dist >= bestd) {
                    i = NET_SIZE /* stop iter */
                } else {
                    i++
                    if (dist < 0) {
                        dist = -dist
                    }
                    a = p[0] - b.toInt()
                    if (a < 0) {
                        a = -a
                    }
                    dist += a
                    if (dist < bestd) {
                        a = p[2] - r.toInt()
                        if (a < 0) {
                            a = -a
                        }
                        dist += a
                        if (dist < bestd) {
                            bestd = dist
                            best = p[3]
                        }
                    }
                }
            }
            if (j >= 0) {
                p = network[j]
                dist = g.toInt() - p!![1] /* inx key - reverse dif */
                if (dist >= bestd) {
                    j = -1 /* stop iter */
                } else {
                    j--
                    if (dist < 0) {
                        dist = -dist
                    }
                    a = p[0] - b.toInt()
                    if (a < 0) {
                        a = -a
                    }
                    dist += a
                    if (dist < bestd) {
                        a = p[2] - r.toInt()
                        if (a < 0) {
                            a = -a
                        }
                        dist += a
                        if (dist < bestd) {
                            bestd = dist
                            best = p[3]
                        }
                    }
                }
            }
        }
        return best
    }

    /**
     * Unbias network to give byte values 0..255 and record position i to prepare for sort
     */
    private fun unbiasnet() {
        for (i in 0 until NET_SIZE) {
            network[i]!![0] = network[i]!![0] shr NET_BIAS_SHIFT
            network[i]!![1] = network[i]!![1] shr NET_BIAS_SHIFT
            network[i]!![2] = network[i]!![2] shr NET_BIAS_SHIFT
            network[i]!![3] = i /* record colour no */
        }
    }

    /**
     * Move adjacent neurons by precomputed alpha*(1-((i-j)^2/[r]^2)) in radpower[|i-j|]
     */
    private fun alterneigh(rad: Int, i: Int, b: Int, g: Int, r: Int) {
        var j = i + 1
        var k = i - 1
        var lo = i - rad
        var hi = i + rad
        var m = 1

        if (lo < -1) {
            lo = -1
        }
        if (hi > NET_SIZE) {
            hi = NET_SIZE
        }

        while (j < hi || k > lo) {
            val a = radpower[m++]
            if (j < hi) {
                val p = network[j++]
                p!![0] -= a * (p!![0] - b) / ALPHA_RADBIAS
                p[1] -= a * (p[1] - g) / ALPHA_RADBIAS
                p[2] -= a * (p[2] - r) / ALPHA_RADBIAS
            }
            if (k > lo) {
                val p = network[k--]
                p!![0] -= a * (p!![0] - b) / ALPHA_RADBIAS
                p[1] -= a * (p[1] - g) / ALPHA_RADBIAS
                p[2] -= a * (p[2] - r) / ALPHA_RADBIAS
            }
        }
    }

    /**
     * Move neuron i towards biased (b,g,r) by factor alpha
     */
    private fun altersingle(alpha: Int, i: Int, b: Int, g: Int, r: Int) {
        /* alter hit neuron */
        val n = network[i]
        n!![0] -= alpha * (n!![0] - b) / INIT_ALPHA
        n[1] -= alpha * (n[1] - g) / INIT_ALPHA
        n[2] -= alpha * (n[2] - r) / INIT_ALPHA
    }

    /**
     * Search for biased BGR values ----------------------------
     */
    private fun contest(b: Int, g: Int, r: Int): Int {
        /* finds closest neuron (min dist) and updates freq */
        /* finds best neuron (min dist-bias) and returns position */
        /* for frequently chosen neurons, freq[i] is high and bias[i] is negative */
        /* bias[i] = gamma*((1/netsize)-freq[i]) */
        var bestd = (1 shl 31).inv()
        var bestbiasd = bestd
        var bestpos = -1
        var bestbiaspos = bestpos

        for (i in 0 until NET_SIZE) {
            val n = network[i]
            var dist = n!![0] - b
            if (dist < 0) {
                dist = -dist
            }
            var a = n[1] - g
            if (a < 0) {
                a = -a
            }
            dist += a
            a = n[2] - r
            if (a < 0) {
                a = -a
            }
            dist += a
            if (dist < bestd) {
                bestd = dist
                bestpos = i
            }
            val biasdist = dist - (bias[i] shr INT_BIAS_SHIFT - NET_BIAS_SHIFT)
            if (biasdist < bestbiasd) {
                bestbiasd = biasdist
                bestbiaspos = i
            }
            val betafreq = freq[i] shr BETA_SHIFT
            freq[i] -= betafreq
            bias[i] += betafreq shl GAMMA_SHIFT
        }

        freq[bestpos] += BETA
        bias[bestpos] -= BETA_GAMMA
        return bestbiaspos
    }

    companion object {
        /**
         * number of colours used
         */
        const val NET_SIZE = 256

        /**
         * four primes near 500 - assume no image has a length so large
         * that it is divisible by all four primes
         */
        const val PRIME1 = 499
        const val PRIME2 = 491
        const val PRIME3 = 487
        const val PRIME4 = 503

        /**
         * minimum size for input image
         */
        const val MIN_PICTURE_BYTES = 3 * PRIME4

        /**
         * Network Definitions
         */
        const val MAX_NET_POS = NET_SIZE - 1
        const val NET_BIAS_SHIFT = 4 /* bias for colour values */
        const val NCYCLES = 100 /* no. of learning cycles */

        /**
         * defs for freq and bias
         */
        const val INT_BIAS_SHIFT = 16 /* bias for fractions */
        const val INT_BIAS = 1 shl INT_BIAS_SHIFT
        const val GAMMA_SHIFT = 10 /* gamma = 1024 */
        const val GAMMA = 1 shl GAMMA_SHIFT
        const val BETA_SHIFT = 10
        const val BETA = INT_BIAS shr BETA_SHIFT /* beta = 1/1024 */
        const val BETA_GAMMA = INT_BIAS shl GAMMA_SHIFT - BETA_SHIFT

        /* defs for decreasing radius factor */
        const val INITRAD = NET_SIZE shr 3
        const val RADIUS_BIAS_SHIFT = 6 /* for 256 cols, radius starts at 32.0 biased by 6 bits */
        const val RADIUS_BIAS = 1 shl RADIUS_BIAS_SHIFT
        const val INIT_RADIUS = INITRAD * RADIUS_BIAS /*
        * and decreases by a factor of 1/30 each cycle*/
        const val RADIUS_DECREASE = 30

        /* defs for decreasing alpha factor */
        const val ALPHA_BIAS_SHIFT = 10 /* alpha starts at 1.0 */
        const val INIT_ALPHA = 1 shl ALPHA_BIAS_SHIFT

        /* radbias and alpharadbias used for radpower calculation */
        const val RADBIAS_SHIFT = 8
        const val RADBIAS = 1 shl RADBIAS_SHIFT
        const val ALPHA_RADBSHIFT = ALPHA_BIAS_SHIFT + RADBIAS_SHIFT
        const val ALPHA_RADBIAS = 1 shl ALPHA_RADBSHIFT
    }

    /**
     * radpower for precomputation
     * Initialise network in range (0,0,0) to (255,255,255) and set parameters
     */
    init {
        network = arrayOfNulls(NET_SIZE)
        for (i in 0 until NET_SIZE) {
            val p = IntArray(4)
            network[i] = p
            Arrays.fill(p, 0, 2, (i shl NET_BIAS_SHIFT + 8) / NET_SIZE)
            freq[i] = INT_BIAS / NET_SIZE /* 1/netsize */
            bias[i] = 0
        }
    }
}
