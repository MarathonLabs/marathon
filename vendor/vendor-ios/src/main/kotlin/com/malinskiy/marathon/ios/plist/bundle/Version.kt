package com.malinskiy.marathon.ios.plist.bundle

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.ios.plist.delegateFor

class Version(delegate: NSDictionary) {
    /**
     * The version of the build that identifies an iteration of the bundle.
     * 
     * This key is a machine-readable string composed of one to three period-separated integers, such as 10.14.1. 
     * The string can only contain numeric characters (0-9) and periods.
     *
     * Each integer provides information about the build version in the format [Major].[Minor].[Patch]:
     *
     *     Major: A major revision number.
     *
     *     Minor: A minor revision number.
     *
     *     Patch: A maintenance release number.
     *
     * You can include more integers but the system ignores them.
     *
     * You can also abbreviate the build version by using only one or two integers, where missing integers in the format are 
     * interpreted as zeros. For example, 0 specifies 0.0.0, 10 specifies 10.0.0, and 10.5 specifies 10.5.0.
     */
    val bundleVersion: String? by delegate.delegateFor("CFBundleVersion")

    /**
     * The release or version number of the bundle.
     * 
     * This key is a user-visible string for the version of the bundle. The required format is three period-separated integers, 
     * such as 10.14.1. The string can only contain numeric characters (0-9) and periods.
     *
     * Each integer provides information about the release in the format [Major].[Minor].[Patch]:
     *
     *     Major: A major revision number.
     *
     *     Minor: A minor revision number.
     *
     *     Patch: A maintenance release number.
     *
     * This key is used throughout the system to identify the version of the bundle.
     */
    val bundleShortVersionString: String? by delegate.delegateFor("CFBundleShortVersionString")

    /**
     * The current version of the Information Property List structure.
     * 
     * Xcode adds this key automatically. Don’t change the value. Haha.
     */
    val bundleInfoDictionaryVersion: String? by delegate.delegateFor("CFBundleInfoDictionaryVersion")

    /**
     * A human-readable copyright notice for the bundle.
     */
    val humanReadableCopyright: String? by delegate.delegateFor("NSHumanReadableCopyright")
}
