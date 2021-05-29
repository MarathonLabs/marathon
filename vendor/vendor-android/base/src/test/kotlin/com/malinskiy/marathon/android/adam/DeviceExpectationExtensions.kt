package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.server.stub.dsl.DeviceExpectation
import com.malinskiy.marathon.android.model.Rotation
import kotlinx.coroutines.delay
import java.io.File

fun DeviceExpectation.property(name: String, value: String) = shell("getprop $name", value)
fun DeviceExpectation.shell(cmd: String, stdout: String, delay: Long? = null) {
    session {
        respondOkay()
        expectShell { "$cmd;echo x$?" }
            .accept()
            .apply { if (delay != null) delay(delay) }
            .respond("${stdout}x0")
    }
}

fun DeviceExpectation.shellFail() {
    session {
        respondTransport(false, "Expected failure")
    }
}

fun DeviceExpectation.installApk(tempDir: File, path: String, mode: String, md5: String, params: String, stdout: String = "Success") {
    pushFile(tempDir, path, mode)
    shell("md5 $path", md5)
    shell("pm install $params $path", stdout)
    shell("rm $path", "")
}

fun DeviceExpectation.pushFile(tempDir: File, path: String, mode: String) {
    val tempFile = File(tempDir, "receive").apply {
        delete()
    }

    session {
        respondOkay()
        expectCmd { "sync:" }.accept()
        expectSend { "$path,$mode" }
            .receiveFile(tempFile)
            .done()
    }
}

fun DeviceExpectation.pullFile(tempDir: File, path: String) {
    val tempFile = File(tempDir, "send").apply {
        delete()
        writeText("X")
    }

    session {
        respondOkay()
        expectCmd { "sync:" }.accept()
        expectStat { path }
        respondStat(size = tempFile.length().toInt())
    }

    session {
        respondOkay()
        expectCmd { "sync:" }.accept()
        expectRecv { "$path" }
            .respondFile(tempFile)
            .respondDoneDone()
    }
}

fun DeviceExpectation.framebuffer(file: File) {
    session {
        respondOkay()
        expectFramebuffer()
            .accept()
            .respondScreencaptureV2(file)
    }
}

fun DeviceExpectation.boot(
    abi: String = "x86",
    sdk: Int = 27,
    initialRotation: Rotation = Rotation.ROTATION_0,
    externalStorage: String = "/sdcard",
    hasScreenRecord: Boolean = true,
) {
    shell(
        "getprop", """
                        [sys.boot_completed]: [1]
                        [ro.product.cpu.abi]: [$abi]
                        [ro.build.version.sdk]: [$sdk]
                        [ro.build.version.codename], [REL]
                        [ro.product.model]: [Android SDK built for x86]
                        [ro.product.manufacturer]: [Google] 
                        [ro.boot.serialno]: [EMULATOR30X6X5X0]
                        
                        x0
                    """.trimIndent()
    )
    shell("dumpsys input", "SurfaceOrientation: ${initialRotation.value}")
    shell("echo \$EXTERNAL_STORAGE", "$externalStorage\r\n")
    shell(
        "ls /system/bin/screenrecord", when (hasScreenRecord) {
            true -> "/system/bin/screenrecord"
            false -> "No such file or directory"
        }
    )
    shell("ls /system/bin/md5", "/system/bin/md5")
    shell(
        "getprop", """
                        [sys.boot_completed]: [1]
                        [ro.product.cpu.abi]: [$abi]
                        [ro.build.version.sdk]: [$sdk]
                        [ro.build.version.codename], [REL]
                        [ro.product.model]: [Android SDK built for x86]
                        [ro.product.manufacturer]: [Google] 
                        [ro.boot.serialno]: [EMULATOR30X6X5X0]
                        
                        x0
                    """.trimIndent()
    )
}
