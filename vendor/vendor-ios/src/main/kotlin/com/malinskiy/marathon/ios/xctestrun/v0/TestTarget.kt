package com.malinskiy.marathon.ios.xctestrun.v0

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.ios.plist.PropertyList
import com.malinskiy.marathon.ios.plist.delegateFor
import com.malinskiy.marathon.ios.plist.optionalDelegateFor
import com.malinskiy.marathon.ios.plist.optionalArrayDelegateFor

open class TestTarget(delegate: NSDictionary) : PropertyList<NSDictionary>(delegate) {
    constructor(
        testBundlePath: String? = null,
        testHostPath: String? = null,
        testingEnvironmentVariables: Map<String, String>,
        uiTargetAppPath: String? = null,
        environmentVariables: Map<String, String>? = null,
        commandLineArguments: Array<String>? = null,
        uiTargetAppEnvironmentVariables: Map<String, String>? = null,
        uiTargetAppCommandLineArguments: Array<String>? = null,
        baselinePath: String? = null,
        skipTestIdentifiers: Array<String>? = null,
        onlyTestIdentifiers: Array<String>? = null,
        useDestinationArtifacts: Boolean? = null,
        testHostBundleIdentifier: String? = null,
        testBundleDestinationRelativePath: String? = null,
        uiTargetAppBundleIdentifier: String? = null,
    ) : this(NSDictionary()) {
        this.testBundlePath = testBundlePath
        this.testHostPath = testHostPath
        this.testingEnvironmentVariables = testingEnvironmentVariables
        uiTargetAppPath?.let { this.uiTargetAppPath = it }
        environmentVariables?.let { this.environmentVariables = it }
        commandLineArguments?.let { this.commandLineArguments = it }
        uiTargetAppEnvironmentVariables?.let { this.uiTargetAppEnvironmentVariables = it }
        uiTargetAppCommandLineArguments?.let { this.uiTargetAppCommandLineArguments = it }
        baselinePath?.let { this.baselinePath = it }
        skipTestIdentifiers?.let { this.skipTestIdentifiers = it }
        onlyTestIdentifiers?.let { this.onlyTestIdentifiers = it }
        useDestinationArtifacts?.let { this.useDestinationArtifacts = it }
        testHostBundleIdentifier?.let { this.testHostBundleIdentifier = it }
        testBundleDestinationRelativePath?.let { this.testBundleDestinationRelativePath = it }
        uiTargetAppBundleIdentifier?.let { this.uiTargetAppBundleIdentifier = it }
    }

    //All optional
    
    /**
     * A path to the test bundle to be tested.
     *
     * The xcodebuild tool will expand the following placeholder strings in the path:
     *
     * `__TESTROOT__` - The directory containing the xctestrun file.
     * `__TESTHOST__` - The test host directory bundle provided by TestHostPath.
     */
    var testBundlePath: String? by delegate.delegateFor("TestBundlePath")

    /**
     * A path to the test host. For framework tests, this should be a path to the xctest command line tool.
     *
     * For application hosted tests, this should be a path the application host.
     * For UI tests, this should be a path to the test runner application that the UI test target produces.
     *
     * The xcodebuild tool will expand the following placeholder strings in the path:
     *
     * `__TESTROOT__` - The directory containing the xctestrun file.
     * `__PLATFORMS__` - The platforms directory in the active Xcode.app.
     */
    var testHostPath: String? by delegate.delegateFor("TestHostPath")

    /**
     * Additional testing environment variables that xcodebuild will provide to the [testHostPath] process.
     *
     * The xcodebuild tool will expand the following placeholder strings in the dictionary values:
     *
     * `__TESTBUNDLE__` - The path to the test bundle. This is expanded to a device path when [useDestinationArtifacts] is set.
     *
     * `__TESTHOST__` - The test host directory provided by [testHostPath].
     *
     * `__TESTROOT__` - The directory containing the xctestrun file.
     *
     * `__PLATFORMS__` - The platforms directory in the active Xcode.app.
     *
     * `__SHAREDFRAMEWORKS__` - The shared frameworks directory in the active Xcode.app.
     *
     */
    var testingEnvironmentVariables: Map<String, String>? by delegate.delegateFor("TestingEnvironmentVariables")

    /**
     * A path to the target application for UI tests.
     *
     * The parameter is mandatory for UI tests only.
     *
     * The xcodebuild tool will expand the following placeholder strings in the path:
     *
     * `__TESTROOT__` - The directory containing the xctestrun file.
     */
    var uiTargetAppPath: String? by delegate.delegateFor("UITargetAppPath")

    /**
     * The environment variables from the scheme test action that xcodebuild will provide to the test host process.
     */
    var environmentVariables: Map<String, String>? by delegate.optionalDelegateFor("EnvironmentVariables")

    /**
     * The command line arguments from the scheme test action that xcodebuild will provide to the test host process.
     */
    var commandLineArguments: Array<String> by delegate.optionalArrayDelegateFor<String>("CommandLineArguments")

    /**
     * The environment variables that xcodebuild will provide to the target application during UI tests.
     */
    var uiTargetAppEnvironmentVariables: Map<String, String>? by delegate.optionalDelegateFor("UITargetAppEnvironmentVariables")

    /**
     * The command line arguments that xcodebuild will provide to the target application during UI tests.
     */
    var uiTargetAppCommandLineArguments: Array<String> by delegate.optionalArrayDelegateFor("UITargetAppCommandLineArguments")

    /**
     * A path to a performance test baseline that xcodebuild will provide to the tests.
     *
     * The xcodebuild tool will expand the following placeholder strings in the path:
     *
     * `__TESTBUNDLE__` - The path to the test bundle. This is expanded to a device path when [useDestinationArtifacts] is set.
     */
    var baselinePath: String? by delegate.optionalDelegateFor("BaselinePath")

    /**
     * An array of test identifiers that xcodebuild should exclude from the test run.
     *
     * Test Identifier Format
     * Identifiers for both Swift and Objective-C tests are:
     *
     *       Test-Class-Name[/Test-Method-Name]
     *
     * To exclude all the tests in a class Example.m, the identifier is just "Example".
     * To exclude one specific test in the class, the identifier is "Example/testExample".
     */
    var skipTestIdentifiers: Array<String> by delegate.optionalArrayDelegateFor<String>("SkipTestIdentifiers")

    /**
     * An array of test identifiers that xcodebuild should include in the test run.
     * All other tests will be excluded from the test run. The format for the identifiers is described above.
     */
    var onlyTestIdentifiers: Array<String> by delegate.optionalArrayDelegateFor<String>("OnlyTestIdentifiers")

    // Advanced section

    /**
     * An optional flag to indicate that xcodebuild should look on the destination for test artifacts. When this flag is set,
     * xcodebuild will not install test artifacts to the destination during testing.
     *
     * [testBundlePath], [testHostPath], and [uiTargetAppPath] should be excluded when this flag is set.
     * Instead, xcodebuild requires the following parameters: [testHostBundleIdentifier], [testBundleDestinationRelativePath] and [uiTargetAppBundleIdentifier]
     */
    var useDestinationArtifacts: Boolean? by delegate.optionalDelegateFor("UseDestinationArtifacts")

    /**
     * A bundle identifier for the test host on the destination.
     * This parameter is mandatory when [useDestinationArtifacts] is set.
     */
    var testHostBundleIdentifier: String? by delegate.optionalDelegateFor("TestHostBundleIdentifier")

    /**
     * A path to the test bundle on the destination. This parameter is mandatory when [useDestinationArtifacts] is set.
     * The xcodebuild tool will expand the following placeholder strings in the path:
     *
     * `__TESTHOST__` - The test host directory bundle on the destination.
     */
    var testBundleDestinationRelativePath: String? by delegate.optionalDelegateFor("TestBundleDestinationRelativePath")

    /**
     * A bundle identifier for the UI target application on the destination.
     * This parameter is mandatory when [useDestinationArtifacts] is set.
     */
    var uiTargetAppBundleIdentifier: String? by delegate.optionalDelegateFor("UITargetAppBundleIdentifier")
}
