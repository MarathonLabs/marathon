package com.malinskiy.marathon.buildsystem.xcresulttool

import com.google.gson.annotations.SerializedName

data class Schema(
    @SerializedName("name") val name: String,
    @SerializedName("signature") val signature: String,
    @SerializedName("types") val types: List<TypeDefinition>,
    @SerializedName("version") val version: Version,
)
