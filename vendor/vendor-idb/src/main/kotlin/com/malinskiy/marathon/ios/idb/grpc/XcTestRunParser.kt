package com.malinskiy.marathon.ios.idb.grpc

import com.dd.plist.NSDictionary
import com.dd.plist.NSString
import com.dd.plist.PropertyListParser
import java.io.File


class XcTestRunParser {
    fun extractArtifacts(file: File): List<String> {
        val root = PropertyListParser.parse(file) as NSDictionary
        val result = mutableListOf<String>()
        for (key in root.allKeys()) {
            val appDict = root.objectForKey(key) as NSDictionary
            if (!appDict.containsKey("TestHostPath")) continue
            val path = appDict.objectForKey("TestHostPath") as NSString
            result.add(path.content.replace("__TESTROOT__", file.parent))
        }
        return result.toList()
    }
}
