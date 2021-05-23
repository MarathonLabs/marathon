package com.malinskiy.marathon.android.model

enum class Rotation(val value: Int) {
    ROTATION_0(0),
    ROTATION_180(2),
    ROTATION_270(3),
    ROTATION_90(1);

    companion object {
        fun of(value: Int): Rotation? {
            return when (value) {
                0 -> ROTATION_0
                1 -> ROTATION_90
                2 -> ROTATION_180
                3 -> ROTATION_270
                else -> null
            }
        }
    }
}
