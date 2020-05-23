package com.malinskiy.marathon.ios.idb.configuration

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.Reader

inline fun <reified T> Gson.fromJson(reader: Reader) = fromJson<T>(reader, object: TypeToken<T>() {}.type)

data class IdbHost(val host: String, val port: Int)

class IdbHostsReader(private val gson: Gson) {
    fun readConfig(file: File) : List<IdbHost>{
        return gson.fromJson<List<IdbHost>>(FileReader(file))
    }
}
