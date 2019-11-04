package io.qameta.allure

/**
 * @author charlie (Dmitry Baev).
 */
enum class SeverityLevel private constructor(private val value: String) {

    BLOCKER("blocker"),
    CRITICAL("critical"),
    NORMAL("normal"),
    MINOR("minor"),
    TRIVIAL("trivial");

    fun value(): String {
        return value
    }
}
