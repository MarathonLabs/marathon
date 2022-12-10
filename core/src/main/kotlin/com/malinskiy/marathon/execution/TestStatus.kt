package com.malinskiy.marathon.execution

enum class TestStatus {
    FAILURE,
    PASSED,
    IGNORED,
    INCOMPLETE,
    ASSUMPTION_FAILURE;

    operator fun plus(value: TestStatus): TestStatus {
        return when (this) {
            FAILURE -> FAILURE
            PASSED -> value
            IGNORED -> IGNORED
            INCOMPLETE -> INCOMPLETE
            ASSUMPTION_FAILURE -> ASSUMPTION_FAILURE
        }
    }
}
