package com.malinskiy.marathon.apple.ios.plist.bundle

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.apple.ios.plist.delegateFor

class OperatingSystemVersion(delegate: NSDictionary) {
    /**
     * The minimum version of the operating system required for the app to run in macOS.
     * 
     * Use this key to indicate the minimum macOS release that your app supports. The App Store uses this key to indicate the macOS releases on which your app can run, and to show compatibility with a person’s Mac.
     *
     * Starting with macOS 11.4, the lowest version number you can specify as the value for the LSMinimumSystemVersion key is:
     *
     *     10 if your app links against the macOS SDK.
     *     10.15 if your app links against the iOS 14.3 SDK (or later) and builds using Mac Catalyst.
     *     11 if your iPad or iPhone app links against the iOS 14.3 SDK (or later) and can run on a Mac with Apple silicon.
     *
     * To specify the minimum version of iOS, iPadOS, tvOS, or watchOS that your app supports, use MinimumOSVersion.
     */
    val lsMinimumSystemVersion: String? by delegate.delegateFor("LSMinimumSystemVersion")
    
    /**
     * The minimum version of the operating system required for the app to run in iOS, iPadOS, tvOS, and watchOS.
     * 
     * The App Store uses this key to indicate the OS releases on which your app can run.
     *
     * Don’t specify MinimumOSVersion in the Info.plist file for apps built in Xcode. It uses the value of the Deployment Target in the General settings pane.
     *
     * For macOS, see LSMinimumSystemVersion.
     */
    val minimumOSVersion: String? by delegate.delegateFor("MinimumOSVersion")

    /**
     * A Boolean value indicating whether the app must run in iOS.
     */
    val lsRequiresIPhoneOS: Boolean? by delegate.delegateFor("LSRequiresIPhoneOS")

    /**
     * A Boolean value that indicates whether the bundle is a watchOS app.
     */
    val watchKitApp: Boolean? by delegate.delegateFor("WKWatchKitApp")
}
