package com.malinskiy.marathon.ios.xctest

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.ios.plist.PropertyList
import com.malinskiy.marathon.ios.plist.arrayDelegateFor
import com.malinskiy.marathon.ios.plist.delegateFor
import com.malinskiy.marathon.ios.plist.optionalPlistDelegateFor
import com.malinskiy.marathon.ios.plist.plistDelegateFor
import com.malinskiy.marathon.ios.plist.plistListDelegateFor

/**
 * Undocumented
 */
class Xctest(delegate: NSDictionary): PropertyList<NSDictionary>(delegate) {
    var bundleExecutable: String? by delegate.delegateFor("CFBundleExecutable")
    var bundleDevelopmentRegion: String? by delegate.delegateFor("CFBundleDevelopmentRegion")
    var bundleIdentifier: String? by delegate.delegateFor("CFBundleIdentifier")
    var bundleInfoDictionaryVersion: String? by delegate.delegateFor("CFBundleInfoDictionaryVersion")
    var bundleName: String? by delegate.delegateFor("CFBundleName")
    var bundlePackageType: String? by delegate.delegateFor("CFBundlePackageType")
    var bundleShortVersionString: String? by delegate.delegateFor("CFBundleShortVersionString")
    var bundleSupportedPlatforms: Array<String> by delegate.arrayDelegateFor("CFBundleSupportedPlatforms", optional = true)
    var bundleVersion: String? by delegate.delegateFor("CFBundleVersion")
    var minimumOSVersion: String? by delegate.delegateFor("MinimumOSVersion")
    var buildMachineOSBuild: String? by delegate.delegateFor("BuildMachineOSBuild")
    
    var containsUITests: Boolean? by delegate.delegateFor("XCTContainsUITests")
}
