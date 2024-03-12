package com.malinskiy.marathon.apple.plist.bundle

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.apple.plist.delegateFor

class Categorization(delegate: NSDictionary) {
    /**
     * The type of bundle.
     *
     * This key consists of a four-letter code for the bundle type.
     * For apps, the code is APPL,
     * for frameworks, it's FMWK,
     * and for bundles, it's BNDL.
     *
     * The default value is derived from the bundle extension or, if it can't be derived, the default value is BNDL.
     */
    val packageType: String? by delegate.delegateFor("CFBundlePackageType")

    /**
     * The category that best describes your app for the App Store.
     */
    val applicationCategoryType: String? by delegate.delegateFor("LSApplicationCategoryType")
}
