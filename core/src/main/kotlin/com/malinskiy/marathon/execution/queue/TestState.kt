package com.malinskiy.marathon.execution.queue


/**
 * TestState accumulates all the information about a single test case for one pool
 */
sealed class TestState {
    data class Added(
        val total: Int, 
        val running: Int = 0,
    ) : TestState()
    
    data class Passing(
        val total: Int,
        val running: Int,
        val done: Int,
    ) : TestState()

    data class Failing(
        val total: Int,
        val running: Int,
        val done: Int,
    ) : TestState()

    data class Failed(
        val total: Int,
        val running: Int,
        val done: Int,
    ) : TestState()

    data class Passed(
        val total: Int,
        val running: Int,
        val done: Int,
    ) : TestState()
}
