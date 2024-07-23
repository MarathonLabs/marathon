package com.malinskiy.marathon.android.executor.listeners.video

import com.malinskiy.marathon.android.executor.listeners.video.ScreenRecorder.Companion.addFileNumberForVideo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.UUID.randomUUID

class ScreenRecorderTest {

    @ParameterizedTest
    @MethodSource("fileNames")
    fun testFileName(fileName: String, counter: Long?, expectedName: String) {
        val finalName = fileName.addFileNumberForVideo(counter)
        assertEquals(expectedName, finalName, "The incorrect final fileName")
    }

    companion object {
        const val extension = ".mp4"
        val testBatchId = randomUUID()
        val currentFileName =
            "svyqrgDQUIUEUuOz7yDOAt7gnRjXaTQM7Ag9JY4uwp9bE8Ms7gTW8jStGADi1lprrkkqB2ivdQH3x4nv1qrIj7Q8JLxjQYAskhh4gioN9SiRf5SOHznEIapXmXFWOWJbkTizzYxggCWhRyjOTq1kI06aNIP1QV7YwzLnMcXbZDIWz6O8FteFfa9MoX065Wd8btsYTJb1LKg3ntaX23031A-$testBatchId$extension"
        @JvmStatic
        fun fileNames() = listOf(
            Arguments.of(currentFileName, null, currentFileName),
            Arguments.of(currentFileName, 9L,
                     "svyqrgDQUIUEUuOz7yDOAt7gnRjXaTQM7Ag9JY4uwp9bE8Ms7gTW8jStGADi1lprrkkqB2ivdQH3x4nv1qrIj7Q8JLxjQYAskhh4gioN9SiRf5SOHznEIapXmXFWOWJbkTizzYxggCWhRyjOTq1kI06aNIP1QV7YwzLnMcXbZDIWz6O8FteFfa9MoX065Wd8btsYTJb1LKg3ntaX230319-$testBatchId$extension"
            ),
            Arguments.of(currentFileName, 99L,
                     "svyqrgDQUIUEUuOz7yDOAt7gnRjXaTQM7Ag9JY4uwp9bE8Ms7gTW8jStGADi1lprrkkqB2ivdQH3x4nv1qrIj7Q8JLxjQYAskhh4gioN9SiRf5SOHznEIapXmXFWOWJbkTizzYxggCWhRyjOTq1kI06aNIP1QV7YwzLnMcXbZDIWz6O8FteFfa9MoX065Wd8btsYTJb1LKg3ntaX230399-$testBatchId$extension"
            ),
            Arguments.of(currentFileName, 999L,
                     "svyqrgDQUIUEUuOz7yDOAt7gnRjXaTQM7Ag9JY4uwp9bE8Ms7gTW8jStGADi1lprrkkqB2ivdQH3x4nv1qrIj7Q8JLxjQYAskhh4gioN9SiRf5SOHznEIapXmXFWOWJbkTizzYxggCWhRyjOTq1kI06aNIP1QV7YwzLnMcXbZDIWz6O8FteFfa9MoX065Wd8btsYTJb1LKg3ntaX230999-$testBatchId$extension"
            ),
        )
    }
}
