package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

enum class HtmlStatus {
    @SerializedName("passed")
    Passed,

    @SerializedName("failed")
    Failed,

    @SerializedName("ignored")
    Ignored
}