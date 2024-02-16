package com.malinskiy.marathon.apple.plist.bundle

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.apple.plist.delegateFor

class LaunchConditions(delegate: NSDictionary) {
    /**
     * The device-related features that your app requires to run.
     * 
     * https://developer.apple.com/documentation/bundleresources/information_property_list/uirequireddevicecapabilities/
     */
    val uiRequiredDeviceCapabilities: Array<String> by delegate.delegateFor("UIRequiredDeviceCapabilities")
}
