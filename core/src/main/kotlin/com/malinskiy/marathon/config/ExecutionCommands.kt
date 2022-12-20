package com.malinskiy.marathon.config

sealed class ExecutionCommand

data class ParseCommand(
    val outputFileName: String?
) : ExecutionCommand()

object MarathonRunCommand : ExecutionCommand()
