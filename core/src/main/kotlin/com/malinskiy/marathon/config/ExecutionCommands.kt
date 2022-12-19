package com.malinskiy.marathon.config

import java.io.File

sealed class ExecutionCommand

data class ParseCommand(
    val outputFile: File?
) : ExecutionCommand()

object MarathonRunCommand : ExecutionCommand()
