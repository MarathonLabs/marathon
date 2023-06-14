package com.malinskiy.marathon.ios.plist.bundle

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.ios.plist.PropertyList

class BundleInfo(delegate: NSDictionary) : PropertyList<NSDictionary>(delegate) {
    val categorization = Categorization(delegate)
    val identification = Identification(delegate)
    val naming = Naming(delegate)
}
