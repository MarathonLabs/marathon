package com.malinskiy.marathon.ios.xctestrun.v2

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.ios.plist.delegateFor
import com.malinskiy.marathon.ios.plist.optionalDelegateFor
import com.malinskiy.marathon.ios.plist.optionalArrayDelegateFor
import com.malinskiy.marathon.ios.xctestrun.v0.TestTarget

class TestTarget : TestTarget {

    constructor(delegate: NSDictionary) : super(delegate)

    constructor(
        name: String,
        //Base
        testBundlePath: String,
        testHostPath: String,
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

        treatMissingBaselinesAsFailures: Boolean? = null,
        uiTargetAppMainThreadCheckerEnabled: Boolean? = null,
        gatherLocalizableStringsData: Boolean? = null,
        dependentProductPaths: Array<String>? = null,
        productModuleName: String? = null,
        systemAttachmentLifetime: String? = null,
        userAttachmentLifetime: String? = null,
        parallelizationEnabled: Boolean? = null,
        testExecutionOrdering: String? = null,
        testLanguage: String? = null,
        testRegion: String? = null,
        isUITestBundle: Boolean? = null,
    ) : super(
        testBundlePath,
        testHostPath,
        testingEnvironmentVariables,
        uiTargetAppPath,
        environmentVariables,
        commandLineArguments,
        uiTargetAppEnvironmentVariables,
        uiTargetAppCommandLineArguments, 
        baselinePath, 
        skipTestIdentifiers, 
        onlyTestIdentifiers, 
        useDestinationArtifacts,
        testHostBundleIdentifier, 
        testBundleDestinationRelativePath, 
        uiTargetAppBundleIdentifier
    ) {
        this.name = name
        treatMissingBaselinesAsFailures?.let { this.treatMissingBaselinesAsFailures = it }
        uiTargetAppMainThreadCheckerEnabled?.let { this.uiTargetAppMainThreadCheckerEnabled = it }
        gatherLocalizableStringsData?.let { this.gatherLocalizableStringsData = it }
        dependentProductPaths?.let { this.dependentProductPaths = it }
        productModuleName?.let { this.productModuleName = it }
        systemAttachmentLifetime?.let { this.systemAttachmentLifetime = it }
        userAttachmentLifetime?.let { this.userAttachmentLifetime = it }
        parallelizationEnabled?.let { this.parallelizationEnabled = it }
        testExecutionOrdering?.let { this.testExecutionOrdering = it }
        testLanguage?.let { this.testLanguage = it }
        testRegion?.let { this.testRegion = it }
        isUITestBundle?.let { this.isUITestBundle = it }
    }

    /**
     * The name of the test target, without the file extension of its build product.
     */
    var name: String by delegate.delegateFor("BlueprintName")

    // Optional section

    /**
     * Whether or not a test failure should be reported for performance test cases which do not have a baseline.
     */
    var treatMissingBaselinesAsFailures: Boolean? by delegate.optionalDelegateFor("TreatMissingBaselinesAsFailures")

    /**
     * Whether or not the Main Thread Checker should be enabled for apps launched during UI tests.
     */
    var uiTargetAppMainThreadCheckerEnabled: Boolean? by delegate.optionalDelegateFor("UITargetAppMainThreadCheckerEnabled")

    /**
     * Whether or not localizable strings data should be gathered for apps launched during UI tests.
     */
    var gatherLocalizableStringsData: Boolean? by delegate.optionalDelegateFor("GatherLocalizableStringsData")

    /**
     * List of paths to the build products of this target and all of its dependencies.
     * Used to determine the bundle identifiers for apps during UI tests.
     */
    var dependentProductPaths: Array<String> by delegate.optionalArrayDelegateFor("DependentProductPaths")

    /**
     * The module name of this test target, as specified by the target's PRODUCT_MODULE_NAME build setting in Xcode.
     */
    var productModuleName: String? by delegate.optionalDelegateFor("ProductModuleName")

    /**
     * How long automatic UI testing screenshots should be kept. Should be one of the following string values:
     *
     *      keepAlways - Always keep attachments, even for tests that succeed.
     *      deleteOnSuccess- Keep attachments for tests that fail, and discard them for tests that succeed.
     *      keepNever - Always discard attachments, regardless of whether the test succeeds or fails.
     */
    var systemAttachmentLifetime: String? by delegate.optionalDelegateFor("SystemAttachmentLifetime")

    /**
     * How long custom file attachments should be kept.
     * Should be one of the string values specified in the [systemAttachmentLifetime].
     */
    var userAttachmentLifetime: String? by delegate.optionalDelegateFor("UserAttachmentLifetime")

    /**
     * Whether or not the tests in this test target should be run in parallel using multiple test runner processes.
     */
    var parallelizationEnabled: Boolean? by delegate.optionalDelegateFor("ParallelizationEnabled")

    /**
     * The order in which tests should be run.
     * By default, tests are run in alphabetical order and this field may be omitted,
     * buttests may be run in a randomized order by specifying this setting with the string value "random".
     */
    var testExecutionOrdering: String? by delegate.optionalDelegateFor("TestExecutionOrdering")

    /**
     * Language identifier code for the language which tests should be run using.
     */
    var testLanguage: String? by delegate.optionalDelegateFor("TestLanguage")

    /**
     * Region identifier code for the region which tests should be run using.
     */
    var testRegion: String? by delegate.optionalDelegateFor("TestRegion")
    
    // Undocumented
    /**
     * 
     */
    var isUITestBundle: Boolean? by delegate.optionalDelegateFor("IsUITestBundle")
}
