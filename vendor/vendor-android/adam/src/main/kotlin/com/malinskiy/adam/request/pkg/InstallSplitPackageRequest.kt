package com.malinskiy.adam.request.pkg

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.annotation.Features
import com.malinskiy.adam.request.Feature
import com.malinskiy.adam.request.MultiRequest
import com.malinskiy.adam.request.ValidationResponse
import com.malinskiy.adam.request.pkg.multi.ApkSplitInstallationPackage
import com.malinskiy.adam.request.pkg.multi.CreateIndividualPackageSessionRequest
import com.malinskiy.adam.request.pkg.multi.InstallCommitRequest
import com.malinskiy.adam.request.pkg.multi.WriteIndividualPackageRequest
import kotlinx.coroutines.Dispatchers
import java.io.File
import kotlin.coroutines.CoroutineContext

/**
 * If both CMD and ABB_EXEC are missing, falls back to exec:pm
 */
@Features(Feature.CMD, Feature.ABB_EXEC)
class InstallSplitPackageRequest(
    private val pkg: ApkSplitInstallationPackage,
    private val supportedFeatures: List<Feature>,
    private val reinstall: Boolean,
    private val extraArgs: List<String> = emptyList(),
    val coroutineContext: CoroutineContext = Dispatchers.IO
) : MultiRequest<Unit>() {

    private val totalSize: Long by lazy {
        pkg.fileList.map { it.length() }.sum()
    }

    override suspend fun execute(androidDebugBridgeClient: AndroidDebugBridgeClient, serial: String?) = with(androidDebugBridgeClient) {
        val sessionId = execute(
            CreateIndividualPackageSessionRequest(
                pkg,
                listOf(pkg),
                supportedFeatures,
                reinstall,
                extraArgs
            ),
            serial
        )
        try {
            for (file in pkg.fileList) {
                execute(WriteIndividualPackageRequest(file, supportedFeatures, sessionId, coroutineContext), serial)
            }
            execute(InstallCommitRequest(sessionId, supportedFeatures), serial)
        } catch (e: Exception) {
            try {
                execute(InstallCommitRequest(sessionId, supportedFeatures, abandon = true), serial)
            } catch (e: Exception) {
                //Ignore
            }
            throw e
        }
    }

    override fun validate(): ValidationResponse {
        val response = super.validate()
        if (!response.success) {
            return response
        } else {
            for (file in pkg.fileList) {
                val message = validateFile(file) ?: continue
                return ValidationResponse(false, message)
            }

            if (!pkg.fileList.any { it.extension == "apk" }) {
                return ValidationResponse(false, ValidationResponse.oneOfFilesShouldBe("apk"))
            }
        }

        return ValidationResponse.Success
    }

    private fun validateFile(file: File): String? {
        return if (!file.exists()) {
            ValidationResponse.packageShouldExist(file)
        } else if (!file.isFile) {
            ValidationResponse.packageShouldBeRegularFile(file)
        } else if (file.extension == "apex") {
            "Apex is not compatible with InstallSplitPackageRequest"
        } else if (!SUPPORTED_EXTENSIONS.contains(file.extension)) {
            ValidationResponse.packageShouldBeSupportedExtension(file, SUPPORTED_EXTENSIONS)
        } else {
            null
        }
    }

    companion object {
        val SUPPORTED_EXTENSIONS = setOf("apk", "dm", "fsv_sig")
    }
}
