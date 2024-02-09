package com.malinskiy.marathon.apple.ios.plist.bundle

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.apple.ios.plist.delegateFor
import com.malinskiy.marathon.apple.ios.plist.optionalArrayDelegateFor

class Localization(delegate: NSDictionary) {
    /**
     * The default language and region for the bundle, as a language ID.
     * 
     * The system uses this key as the language if it can't locate a resource for the user’s preferred language. 
     * The value should be a language ID that identifies a language, dialect, or script.
     */
    val developmentRegion: String? by delegate.delegateFor("CFBundleDevelopmentRegion")
    /**
     * The localizations handled manually by your app.
     */
    val localizations: Array<String> by delegate.optionalArrayDelegateFor("CFBundleLocalizations")

    /**
     * A Boolean value that indicates whether the bundle supports the retrieval of localized strings from frameworks.
     */
    val allowMixedLocalizations: Boolean? by delegate.delegateFor("CFBundleAllowMixedLocalizations")

    /**
     * A Boolean value that enables the Caps Lock key to switch between Latin and non-Latin input sources.
     */
    val capsLockLanguageSwitchCapable: Boolean? by delegate.delegateFor("TICapsLockLanguageSwitchCapable")

}
