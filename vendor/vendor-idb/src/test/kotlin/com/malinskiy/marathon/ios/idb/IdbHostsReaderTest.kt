package com.malinskiy.marathon.ios.idb

import com.google.gson.Gson
import com.malinskiy.marathon.ios.idb.configuration.IdbHost
import com.malinskiy.marathon.ios.idb.configuration.IdbHostsReader
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test

class IdbHostsReaderTest {
    @Test
    fun readConfiguration() {
        val reader = IdbHostsReader(Gson())
        val result = reader.readConfig("src/test/resources/Marathondevices.json")
        result shouldEqual listOf(
            IdbHost("localhost", 10882),
            IdbHost("localhost", 10882)
        )
    }
}
