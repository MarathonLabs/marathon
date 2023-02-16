package com.malinskiy.marathon.config.strategy

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @property fast fail-fast or success-fast depending on the value of [mode]. This doesn't affect the result
 *  of the run, it only saves compute time
 */
data class ExecutionStrategyConfiguration(
    @JsonProperty("mode") val mode: ExecutionMode = ExecutionMode.ANY_SUCCESS,
    @JsonProperty("fast") val fast: Boolean = true,
)

/**
 * @property ANY_SUCCESS test passes if any of executions is passing
 *  this mode works only if there is no complex sharding strategy applied
 *  
 *  Why: it doesn't make sense when user asks for N number of tests
 *  to run explicitly, and we pass on the first one. Sharding used to verify probability of passing with an explicit boundary for precision
 * @property ALL_SUCCESS test passes if and only if all the executions are passing
 *  this mode works only if there are no retries, i.e. no complex flakiness strategy, no retry strategy
 *  
 *  Why: when adding retries to tests with retry+flakiness strategies users want to trade-off cost for realiability, i.e. add more retries
 *  and pass if one of them passes, so retries only make sense for the [ANY_SUCCESS] mode. When we use [ALL_SUCCESS] mode it means user
 *  wants to verify each test with a number of tries (they are not retries per se) and pass only if all of them succeed. This is the case
 *  when fixing a flaky test or adding a new test, and we want to have a signal that the test is fixed/not flaky.
 */
enum class ExecutionMode {
    @JsonProperty("ANY_SUCCESS") ANY_SUCCESS,
    @JsonProperty("ALL_SUCCESS") ALL_SUCCESS,
}
