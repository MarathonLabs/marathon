package com.malinskiy.marathon.android.ddmlib

import com.android.ddmlib.AdbCommandRejectedException
import com.android.ddmlib.IDevice
import com.android.ddmlib.IShellOutputReceiver
import com.android.ddmlib.InstallException
import com.android.ddmlib.InstallReceiver
import com.android.ddmlib.MultiLineReceiver
import com.android.ddmlib.ShellCommandUnresponsiveException
import com.android.ddmlib.TimeoutException
import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.ADB_INSTALL_TIMEOUT_MINUTES
import com.malinskiy.marathon.android.ADB_SCREEN_RECORD_TIMEOUT_MINUTES
import com.malinskiy.marathon.android.ADB_SHORT_TIMEOUT_SECONDS
import com.malinskiy.marathon.test.Test
import java.io.IOException
import java.util.concurrent.TimeUnit


fun IDevice.safeUninstallPackage(packageName: String, keepData: Boolean): String? {
    try {
        val receiver = InstallReceiver()
        val cmd = if (keepData) {
            "pm uninstall -k $packageName"
        } else {
            "pm uninstall $packageName"
        }


        executeShellCommand(
            cmd,
            receiver,
            ADB_INSTALL_TIMEOUT_MINUTES,
            ADB_INSTALL_TIMEOUT_MINUTES,
            TimeUnit.MINUTES
        )

        return receiver.errorMessage
    } catch (e: TimeoutException) {
        throw InstallException(e)
    } catch (e: AdbCommandRejectedException) {
        throw InstallException(e)
    } catch (e: ShellCommandUnresponsiveException) {
        throw InstallException(e)
    } catch (e: IOException) {
        throw InstallException(e)
    }
}

fun IDevice.safeInstallPackage(packageFilePath: String, reinstall: Boolean, vararg extraArgs: String): String? {
    val receiver = InstallReceiver()

    installPackage(
        packageFilePath,
        reinstall,
        receiver,
        ADB_INSTALL_TIMEOUT_MINUTES,
        ADB_INSTALL_TIMEOUT_MINUTES,
        TimeUnit.MINUTES,
        *extraArgs
    )

    return receiver.errorMessage
}

fun IDevice.safeExecuteShellCommand(command: String, receiver: IShellOutputReceiver) {
    executeShellCommand(command, receiver, ADB_SHORT_TIMEOUT_SECONDS, ADB_SHORT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
}

fun IDevice.safeStartScreenRecorder(
    remoteFilePath: String,
    options: com.malinskiy.marathon.android.executor.listeners.video.ScreenRecorderOptions,
    receiver: IShellOutputReceiver
) {
    val screenRecorderCommand = options.toScreenRecorderCommand(remoteFilePath)
    executeShellCommand(
        screenRecorderCommand,
        receiver,
        ADB_SCREEN_RECORD_TIMEOUT_MINUTES,
        ADB_SCREEN_RECORD_TIMEOUT_MINUTES,
        TimeUnit.MINUTES
    )
}

fun IDevice.safeClearPackage(packageName: String): String? {
    var result: String? = null

    try {
        val receiver = SimpleOutputReceiver()
        executeShellCommand(
            "pm clear $packageName",
            receiver,
            ADB_SHORT_TIMEOUT_SECONDS,
            ADB_SHORT_TIMEOUT_SECONDS,
            TimeUnit.SECONDS
        )

        result = receiver.output()
    } catch (e: TimeoutException) {
    } catch (e: AdbCommandRejectedException) {
    } catch (e: ShellCommandUnresponsiveException) {
    } catch (e: IOException) {
    } finally {
        return result
    }
}

class SimpleOutputReceiver : MultiLineReceiver() {
    private val buffer = StringBuffer()

    fun output() = buffer.toString()

    override fun processNewLines(lines: Array<out String>?) {
        lines?.forEach {
            buffer.append(it)
        }
    }

    override fun isCancelled() = false
}

fun TestIdentifier.toTest(): Test {
    val pkg = className.substringBeforeLast(".")
    val className = className.substringAfterLast(".")
    val methodName = testName
    return Test(pkg, className, methodName, emptyList())
}
