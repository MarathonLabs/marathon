package com.malinskiy.marathon.ios.xctestrun.v2

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.ios.plist.PropertyList
import com.malinskiy.marathon.ios.plist.delegateFor

class TestPlan(delegate: NSDictionary) : PropertyList<NSDictionary>(delegate) {

    /**
     * The name of the test plan this xctestrun file was generated from.
     */
    var name: String by delegate.delegateFor("Name")

    /**
     * Whether the test plan this xctestrun file was generated from is the default in the scheme.
     */
    var isDefault: Boolean by delegate.delegateFor("IsDefault")
    
    
    constructor(name: String, isDefault: Boolean) : this(NSDictionary()) {
        this.name = name
        this.isDefault = isDefault
    }
}
