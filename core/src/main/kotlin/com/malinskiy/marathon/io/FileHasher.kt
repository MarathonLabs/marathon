package com.malinskiy.marathon.io

import java.io.File

interface FileHasher {
    suspend fun getHash(file: File): String
}
