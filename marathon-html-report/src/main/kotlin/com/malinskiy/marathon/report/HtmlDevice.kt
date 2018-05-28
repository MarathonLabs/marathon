package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

data class HtmlDevice(@SerializedName("id") val id: String,
                      @SerializedName("model") val model: String,
                      @SerializedName("logcat_path") val logcatPath: String,
                      @SerializedName("instrumentation_output_path") val instrumentationOutputPath: String)