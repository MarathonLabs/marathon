package com.malinskiy.marathon.ios.idb.grpc

import com.soywiz.korio.stream.readBytesUpTo
import com.soywiz.korio.stream.toAsync
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

    private fun generateTarCommand(input: File, output: Path): ProcessBuilder {
        return ProcessBuilder().apply {
            command("tar", "-cf", output.toString(), input.path.toString())
        }
    }

    private fun generateTar(input: File): Path {
        val name = input.nameWithoutExtension
        val output = Files.createTempFile(name, "tar")
        val builder = generateTarCommand(input, output)
        builder.start().waitFor()
        return output
    }

    fun generateChunks(file: File): Flow<ByteArray> {
        return when (file.extension) {
            "ipa" -> {
                split(file)
            }
            "app" -> {
                val tar = generateTar(file)
                split(tar.toFile())
            }
            else -> {
                throw IllegalArgumentException()
            }
        }
    }
}
