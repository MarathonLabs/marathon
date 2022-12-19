package com.malinskiy.marathon.buildsystem.xcresulttool

import com.google.gson.annotations.SerializedName

data class Type(
    @SerializedName("name") val name: String,
    @SerializedName("supertype") val supertype: String?,
)
