package com.malinskiy.marathon.ios.plist.bundle

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.ios.plist.delegateFor

class Help(delegate: NSDictionary) {
    /**
     * The name of the bundle’s HTML help file.
     */
    val appleHelpAnchor: String? by delegate.delegateFor("CFAppleHelpAnchor")

    /**
     * The name of the help file that will be opened in Help Viewer.
     */
    val helpBookName: String? by delegate.delegateFor("CFBundleHelpBookName")

    /**
     * The name of the folder containing the bundle’s help files.
     */
    val helpBookFolder: String? by delegate.delegateFor("CFBundleHelpBookFolder")
}
