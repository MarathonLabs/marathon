package com.malinskiy.marathon.ios.xctestrun.v2

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.ios.plist.PropertyList
import com.malinskiy.marathon.ios.plist.delegateFor
import com.malinskiy.marathon.ios.plist.plistListDelegateFor

class TestConfiguration(delegate: NSDictionary) : PropertyList<NSDictionary>(delegate) {

    constructor(name: String, testTargets: Array<TestTarget>) : this(NSDictionary()) {
        this.name = name
        this.testTargets = testTargets
    }
    
    /**
     * The name of the configuration. This name should be unique among the dictionaries in the TestConfigurations array.
     */
    var name: String by delegate.delegateFor("Name")

    /**
     * List of test targets to include in the test configuration. 
     * Each test target contains information about how to test a particular test bundle 
     */
    var testTargets: Array<TestTarget> by delegate.plistListDelegateFor("TestTargets", TestTarget::class, false)
}
