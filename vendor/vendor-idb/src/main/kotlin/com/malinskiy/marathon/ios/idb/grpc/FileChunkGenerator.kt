package com.malinskiy.marathon.ios.idb.grpc

import com.soywiz.korio.stream.readBytesUpTo
import com.soywiz.korio.stream.toAsync
import idb.InstallRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

private const val CHUNK_SIZE = 16384

class FileChunkGenerator {
    private fun split(file: File) = flow {
        val stream = file.inputStream().toAsync()
        while (true) {
            val bytes = stream.readBytesUpTo(CHUNK_SIZE)
            if (bytes.isEmpty()) {
                break
            }
            emit(bytes)
        }
    }

    private fun generateTarCommand(input: List<File>, output: Path): ProcessBuilder {
        val paths = input.map { it.path }.toTypedArray()
        return ProcessBuilder().apply {
            command("tar", "-cf", output.toString(), *paths)
        }
    }

    private fun generateTar(input: File): Path {
        return generateTar(listOf(input))
    }

    private fun generateTar(input: List<File>): Path {
        val output = Files.createTempFile("output", "tar")
        val builder = generateTarCommand(input, output)
        builder.start().waitFor()
        return output
    }

    fun generateChunks(destination: InstallRequest.Destination, file: File): Flow<ByteArray> {
        return when (destination) {
            InstallRequest.Destination.APP -> {
                handleApp(file)
            }
            InstallRequest.Destination.XCTEST -> {
                handleXcTest(file)
            }
            else -> throw IllegalArgumentException("Destination: ${destination.name} is not supported")
        }
    }

    private fun handleXcTest(file: File): Flow<ByteArray> {
        val files = XcTest().parse(file)
        val tar = generateTar(files)
        return split(tar.toFile())
    }

    private fun handleApp(file: File): Flow<ByteArray> {
        return when (file.extension) {
            "ipa" -> {
                split(file)
            }
            "app" -> {
                val tar = generateTar(file)
                split(tar.toFile())
            }
            else -> throw IllegalArgumentException()
        }
    }
}
