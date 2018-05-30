package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

data class HtmlIndex(@SerializedName("suites") val suites: List<HtmlSuite>)
