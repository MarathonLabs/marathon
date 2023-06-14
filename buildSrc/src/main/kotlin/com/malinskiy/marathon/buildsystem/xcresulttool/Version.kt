package com.malinskiy.marathon.buildsystem.xcresulttool

import com.google.gson.annotations.SerializedName

data class Version(
    @SerializedName("major") val major: Int,
    @SerializedName("minor") val minor: Int,
)
