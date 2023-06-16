package com.malinskiy.marathon.android.adam.extension

import com.malinskiy.adam.request.shell.v1.ShellCommandResult

fun ShellCommandResult.toShellResult(): com.malinskiy.marathon.android.model.ShellCommandResult {
    return com.malinskiy.marathon.android.model.ShellCommandResult(output, exitCode)
}
