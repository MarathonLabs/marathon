package com.malinskiy.marathon.android.model

enum class Rotation {
    ROTATION_0,
    ROTATION_180,
    ROTATION_270,
    ROTATION_90;

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
