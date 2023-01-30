package com.malinskiy.marathon.android.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.malinskiy.marathon.execution.TestStatus
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class AndroidTestStatusTest {
    @ParameterizedTest
    @EnumSource(AndroidTestStatus::class)
    fun testAll(status: AndroidTestStatus) {
        assertThat(status.toMarathonStatus()).isEqualTo(
            when (status) {
                AndroidTestStatus.FAILURE -> TestStatus.FAILURE
                AndroidTestStatus.PASSED -> TestStatus.PASSED
                AndroidTestStatus.INCOMPLETE -> TestStatus.INCOMPLETE
                AndroidTestStatus.ASSUMPTION_FAILURE -> TestStatus.ASSUMPTION_FAILURE
                AndroidTestStatus.IGNORED -> TestStatus.IGNORED
            }
        )
    }
}
