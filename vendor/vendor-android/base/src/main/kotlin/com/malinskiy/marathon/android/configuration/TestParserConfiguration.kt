package com.malinskiy.marathon.android.configuration

sealed class TestParserConfiguration {
    object LocalTestParser : TestParserConfiguration()
    data class RemoteTestParser(
        val instrumentationArgs: Map<String, String> = emptyMap(),
    ) : TestParserConfiguration()
}
