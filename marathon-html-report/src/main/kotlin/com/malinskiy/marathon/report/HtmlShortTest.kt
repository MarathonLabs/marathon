package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

data class HtmlShortTest(@SerializedName("id") val id: String,
                         @SerializedName("package_name") val packageName: String,
                         @SerializedName("class_name") val className: String,
                         @SerializedName("name") val name: String,
                         @SerializedName("duration_millis") val durationMillis: Long,
                         @SerializedName("status") val status: HtmlStatus,
                         @SerializedName("deviceId") val deviceId: String,
                         @SerializedName("deviceModel") val deviceModel: String,
                         @SerializedName("properties") val properties: Map<String, Any>
)

fun HtmlFullTest.toHtmlShortTest() = HtmlShortTest(
        id = id,
        packageName = packageName,
        className = className,
        name = name,
        durationMillis = durationMillis,
        status = status,
        deviceId = deviceId,
        deviceModel = deviceModel,
        properties = properties
)
