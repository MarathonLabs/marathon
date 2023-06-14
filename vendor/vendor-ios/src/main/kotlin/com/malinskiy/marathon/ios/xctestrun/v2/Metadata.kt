package com.malinskiy.marathon.ios.xctestrun.v2

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.ios.plist.PropertyList
import com.malinskiy.marathon.ios.plist.delegateFor

class Metadata(delegate: NSDictionary) : PropertyList<NSDictionary>(delegate) {
    constructor(formatVersion: Int) : this(NSDictionary()) {
        this.formatVersion = formatVersion
    }
    
    /**
     * The version of the xctestrun file format. Currently equal to 2.
     * This must be specified in order for xcodebuild to interpret the xctestrun file correctly for the version indicated.
     */
    var formatVersion: Int by delegate.delegateFor("FormatVersion")
}
