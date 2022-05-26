package com.example

import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

class FilePushTest {

    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testExternalFilePresent() {
        val contents = File("/data/local/tmp/external-file").readText()
        assertTrue(contents.trim() == "cafebabe")
    }
    
    @Test
    fun testExternalFolderPresent() {
        val contents = File("/data/local/tmp/external-folder/external-file-2").readText()
        assertTrue(contents.trim() == "abracadabra")
    }
}
