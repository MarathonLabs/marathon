package com.malinskiy.marathon.apple.plist.bundle

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.apple.plist.delegateFor

class Naming(delegate: NSDictionary) {
    /**
     * A user-visible short name for the bundle.
     *
     * This name can contain up to 15 characters. The system may display it to users if CFBundleDisplayName isn't set.
     */
    val bundleName: String? by delegate.delegateFor("CFBundleName")

    /**
     * The user-visible name for the bundle, used by Siri and visible on the iOS Home screen.
     *
     * Use this key if you want a product name that's longer than CFBundleName.
     */
    val bundleDisplayName: String? by delegate.delegateFor("CFBundleDisplayName")

    /**
     * A replacement for the app name in text-to-speech operations.
     */
    val bundleSpokenName: String? by delegate.delegateFor("CFBundleSpokenName")
}
