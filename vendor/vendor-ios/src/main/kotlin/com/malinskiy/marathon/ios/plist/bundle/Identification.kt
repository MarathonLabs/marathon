package com.malinskiy.marathon.ios.plist.bundle

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.ios.plist.delegateFor

class Identification(delegate: NSDictionary) {
    /**
     * A unique identifier for a bundle.
     *
     * A bundle ID uniquely identifies a single app throughout the system.
     * The bundle ID string must contain only alphanumeric characters (A–Z, a–z, and 0–9), hyphens (-), and periods (.).
     * Typically, you use a reverse-DNS format for bundle ID strings. Bundle IDs are case-insensitive.
     */
    val bundleIdentifier: String? by delegate.delegateFor("CFBundleIdentifier")

    /**
     * The bundle ID of the watchOS app.
     *
     * This key is automatically included in your WatchKit extension’s information property list 
     * when you create a watchOS project from a template.
     */
    val watchKitBundleIdentifier: String? by delegate.delegateFor("WKAppBundleIdentifier")

    /**
     * The bundle ID of the watchOS app’s companion iOS app.
     *
     * Xcode automatically includes this key in the WatchKit app’s information property list 
     * when you create a watchOS project from a template. The value should be the same as the iOS app’s CFBundleIdentifier.
     */
    val watchKitCompanionAppBundleIdentifier: String? by delegate.delegateFor("WKCompanionAppBundleIdentifier")
}
