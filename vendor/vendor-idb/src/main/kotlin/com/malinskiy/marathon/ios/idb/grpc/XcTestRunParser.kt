package com.malinskiy.marathon.ios.idb.grpc

import com.dd.plist.NSArray
import com.dd.plist.NSDictionary
import com.dd.plist.PropertyListParser
import java.io.File


class XcTestRunParser {
    fun extractArtifacts(file: File): List<String> {
        val root = PropertyListParser.parse(file) as NSDictionary
        for (key in root.allKeys()) {
            val appDict = root.objectForKey(key) as NSDictionary
            if (!appDict.containsKey("DependentProductPaths")) continue
            val dependentArray = appDict.objectForKey("DependentProductPaths") as NSArray
            return dependentArray.array.map {
                it.toJavaObject() as String
            }.map {
                it.replace("__TESTROOT__", file.parent)
            }
        }
        throw RuntimeException("Cannot find DependentProductPaths")
    }
}
