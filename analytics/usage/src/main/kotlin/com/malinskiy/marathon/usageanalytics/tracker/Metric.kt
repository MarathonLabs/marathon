package com.malinskiy.marathon.usageanalytics.tracker

data class Metric(
    val name: String,
    val value: Number,
    val time: Long = System.currentTimeMillis() / 1000,
    val interval: Int = 1,
    val tags: Map<String, String> = emptyMap(),
) {
    fun toJson() = """{"name":"$name","interval":$interval,"value":$value,"time":$time,"tags":[${
        tags.map { "${it.key}=${it.value}" }.joinToString(separator = ",") { "\"$it\""}
    }]}"""
}
