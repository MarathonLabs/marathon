package com.malinskiy.marathon.buildsystem.xcresulttool

import com.google.gson.annotations.SerializedName

data class TypeDefinition(
    @SerializedName("kind") val kind: String,
    @SerializedName("properties") val properties: List<Property>,
    @SerializedName("type") val type: Type,
)
