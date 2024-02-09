package com.malinskiy.marathon.apple.ios.plist.bundle

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.apple.ios.plist.delegateFor

class Launch(delegate: NSDictionary) {
    /**
     * The name of the bundle’s executable file.
     * 
     * For an app, this key is the executable. For a loadable bundle, it's the binary that's loaded dynamically by the bundle. 
     * For a framework, it's the shared library framework and must have the same name as the framework but without the .framework extension.
     *
     * macOS uses this key to locate the bundle’s executable or shared library in cases where the user renames the app or bundle directory.
     */
    val bundleExecutable: String? by delegate.delegateFor("CFBundleExecutable")
}
