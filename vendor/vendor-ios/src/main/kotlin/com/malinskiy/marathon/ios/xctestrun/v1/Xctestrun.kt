package com.malinskiy.marathon.ios.xctestrun.v1

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.ios.plist.PropertyList
import com.malinskiy.marathon.ios.plist.arrayDelegateFor
import com.malinskiy.marathon.ios.plist.delegateFor

class Xctestrun(delegate: NSDictionary) : PropertyList<NSDictionary>(delegate) {
    val targets: Map<String, XctestrunTarget> by lazy {
        delegate.filterKeys { it != "__xctestrun_metadata__" && delegate[it] is NSDictionary }.mapValues {
            XctestrunTarget((delegate[it.key] as NSDictionary))
        }
    }
    val metadata: Map<String, Any>? by delegate.delegateFor("__xctestrun_metadata__")
}

class XctestrunTarget(delegate: NSDictionary) : PropertyList<NSDictionary>(delegate) {
    var productModuleName: String by delegate.delegateFor("ProductModuleName")
    var isUITestBundle: Boolean by delegate.delegateFor("IsUITestBundle")
    var testBundlePath: String by delegate.delegateFor("TestBundlePath")
    var testHostPath: String by delegate.delegateFor("TestHostPath")
    var uiTargetAppPath: String by delegate.delegateFor("UITargetAppPath")
    var dependentProductPaths: Array<String> by delegate.arrayDelegateFor("DependentProductPaths", false)
    var userAttachmentLifetime: String by delegate.delegateFor("UserAttachmentLifetime")
    var systemAttachmentLifetime: String by delegate.delegateFor("SystemAttachmentLifetime")
    var environmentVariables: Map<String, String> by delegate.delegateFor("EnvironmentVariables")
    var testingEnvironmentVariables: Map<String, String> by delegate.delegateFor("TestingEnvironmentVariables")
}

