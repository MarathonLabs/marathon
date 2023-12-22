package com.malinskiy.marathon.config

sealed class ExecutionCommand

data class ParseCommand(
    val outputFileName: String?,
    val includeFlakyTests: Boolean = false,
) : ExecutionCommand()

object MarathonRunCommand : ExecutionCommand()
