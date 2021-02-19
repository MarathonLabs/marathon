package com.malinskiy.marathon.vendor.junit4.model

import com.malinskiy.marathon.execution.TestStatus

enum class JUnit4TestStatus {
    /** Test failed.  */
    FAILURE,

    /** Test passed  */
    PASSED,

    /** Test started but not ended  */
    INCOMPLETE,

    /** Test assumption failure  */
    ASSUMPTION_FAILURE,

    /** Test ignored  */
    IGNORED;

    fun toMarathonStatus(): TestStatus = when (this) {
        PASSED -> TestStatus.PASSED
        FAILURE -> TestStatus.FAILURE
        IGNORED -> TestStatus.IGNORED
        INCOMPLETE -> TestStatus.INCOMPLETE
        ASSUMPTION_FAILURE -> TestStatus.ASSUMPTION_FAILURE
    }
}
