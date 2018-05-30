package com.malinskiy.marathon.report

import com.google.gson.annotations.SerializedName

data class HtmlFullTest(@SerializedName("suite_id") val suiteId: String,
                        @SerializedName("package_name") val packageName: String,
                        @SerializedName("class_name") val className: String,
                        @SerializedName("name") val name: String,
                        @SerializedName("id") val id: String = "$packageName$className$name",
                        @SerializedName("duration_millis") val durationMillis: Long,
                        @SerializedName("status") val status: HtmlStatus,
                        @SerializedName("stacktrace") val stacktrace: String?,
                        @SerializedName("logcat_path") val logcatPath: String,
                        @SerializedName("deviceId") val deviceId: String,
                        @SerializedName("deviceModel") val deviceModel: String,
                        @SerializedName("properties") val properties: Map<String, Any>,
                        @SerializedName("file_paths") val filePaths: List<String>,
                        @SerializedName("screenshots") val screenshots: List<Screenshot>
) {
    data class Screenshot(
            @SerializedName("path")
            val path: String,

            @SerializedName("title")
            val title: String
    )
}