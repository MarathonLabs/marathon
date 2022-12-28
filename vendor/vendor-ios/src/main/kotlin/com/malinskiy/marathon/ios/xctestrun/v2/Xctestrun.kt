package com.malinskiy.marathon.ios.xctestrun.v2

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.ios.plist.PropertyList
import com.malinskiy.marathon.ios.plist.optionalPlistDelegateFor
import com.malinskiy.marathon.ios.plist.plistDelegateFor
import com.malinskiy.marathon.ios.plist.plistListDelegateFor

/**
 *  V2 of xctestrun format
 *  
 *  Man-page reference from July 1, 2019:
 *  NAME
 *      xcodebuild.xctestrun - Test run parameters files for xcodebuild
 *
 * DESCRIPTION
 *      This document details the parameters contained in an xctestrun file. During the build-for-testing action, xcodebuild extracts
 *      parameters from the scheme provided to the build-for-testing action and writes the parameters to an xctestrun file in the built
 *      products directory. A developer can edit the parameters in the xctestrun file and then provide the file to the xcodebuild
 *      test-without-building action with the -xctestrun option.
 *
 * VERSION
 *      This document describes version 2 of the xctestrun file format, which is supported by Xcode 11 and later and includes support for
 *      running a set of test targets multiple times using test configurations.
 *
 * PROPERTY LIST STRUCTURE
 *      An xctestrun file is formatted as a property list organized using the following top-level structure:
 *
 *            o   TestPlan <dictionary>
 *                      o   Name <string>
 *                      o   IsDefault <bool>
 *
 *            o   TestConfigurations <array>
 *                      o   Item 0 <dictionary>
 *                                o   Name <string>
 *                                o   TestTargets <array>
 *                                          o   Item 0 <dictionary>
 *                                                    o   (Test target keys...)
 *                                          o   ...
 *                                          o   Item N <dictionary>
 *
 *                      o   ...
 *                      o   Item N <dictionary>
 *
 *            o   CodeCoverageBuildableInfos <array>
 *                      o   Item 0 <dictionary>
 *                                o   (Code coverage buildable info keys...)
 *                      o   ...
 *                      o   Item N <dictionary>
 *
 *            o   __xctestrun_metadata__ <dictionary>
 *                      o   FormatVersion = 2 <number>
 *
 *      See the sections below for information about each top-level item.
 *
 * TEST PLAN SECTION
 *      The top-level TestPlan dictionary contains metadata about the test plan which was used to construct this xctestrun file. It is
 *      provided for informational purposes and to allow distinguishing between xctestrun files if multiple were generated from a single
 *      scheme. The keys in this dictionary are not used when performing tests.
 *
 *    Name
 *      The name of the test plan this xctestrun file was generated from.
 *
 *    IsDefault
 *      Whether the test plan this xctestrun file was generated from is the default in the scheme.
 *
 * TEST CONFIGURATIONS SECTION
 *      The top-level TestConfigurations array contains the list of test configurations to use when testing. Each entry is a dictionary
 *      containing metadata and a list of test targets to include.
 *
 *    Name
 *      The name of the configuration. This name should be unique among the dictionaries in the TestConfigurations array.
 *
 *    TestTargets
 *      An array containing the list of test targets to include in the test configuration. Each test target is a dictionary containing
 *      information about how to test a particular test bundle, and can contain many different parameters, as described below.
 *
 *      The following parameters are mandatory during basic commands:
 *
 *            BlueprintName <string>
 *            The name of the test target, without the file extension of its build product.
 *
 *            TestBundlePath <string>
 *            A path to the test bundle to be tested. The xcodebuild tool will expand the following placeholder strings in the path:
 *
 *                  __TESTROOT__
 *                  __TESTHOST__
 *
 *            TestHostPath <string>
 *            A path to the test host. For framework tests, this should be a path to the xctest command line tool. For application hosted
 *            tests, this should be a path the application host. For UI tests, this should be a path to the test runner application that the
 *            UI test target produces. The xcodebuild tool will expand the following placeholder strings in the path:
 *
 *                  __TESTROOT__
 *                  __PLATFORMS__
 *
 *            UITargetAppPath <string>
 *            A path to the target application for UI tests. The parameter is mandatory for UI tests only. The xcodebuild tool will expand
 *            the following placeholder strings in the path:
 *
 *                  __TESTROOT__
 *
 *      These parameters are optional for all commands:
 *
 *            EnvironmentVariables <dictionary of string keys and values>
 *            The environment variables from the scheme test action that xcodebuild will provide to the test host process.
 *
 *            CommandLineArguments <array of string values>
 *            The command line arguments from the scheme test action that xcodebuild will provide to the test host process.
 *
 *            UITargetAppEnvironmentVariables <dictionary of string keys and values>
 *            The environment variables that xcodebuild will provide to the target application during UI tests.
 *
 *            UITargetAppCommandLineArguments <array of string values>
 *            The command line arguments that xcodebuild will provide to the target application during UI tests.
 *
 *            BaselinePath <string>
 *            A path to a performance test baseline that xcodebuild will provide to the tests. The xcodebuild tool will expand the following
 *            placeholder strings in the path:
 *
 *                  __TESTBUNDLE__
 *
 *            TreatMissingBaselinesAsFailures <bool>
 *            Whether or not a test failure should be reported for performance test cases which do not have a baseline.
 *
 *            SkipTestIdentifiers <array of strings>
 *            An array of test identifiers that xcodebuild should exclude from the test run.
 *
 *                  Test Identifier Format
 *                  Identifiers for both Swift and Objective-C tests are:
 *
 *                        Test-Class-Name[/Test-Method-Name]
 *
 *                  To exclude all the tests in a class Example.m, the identifier is just "Example". To exclude one specific test in the
 *                  class, the identifier is "Example/testExample".
 *
 *            OnlyTestIdentifiers <array of strings>
 *            An array of test identifiers that xcodebuild should include in the test run. All other tests will be excluded from the test
 *            run. The format for the identifiers is described above.
 *
 *            UITargetAppMainThreadCheckerEnabled <bool>
 *            Whether or not the Main Thread Checker should be enabled for apps launched during UI tests.
 *
 *            GatherLocalizableStringsData <bool>
 *            Whether or not localizable strings data should be gathered for apps launched during UI tests.
 *
 *            DependentProductPaths <array of string values>
 *            List of paths to the build products of this target and all of its dependencies. Used to determine the bundle identifiers for
 *            apps during UI tests.
 *
 *            ProductModuleName <string>
 *            The module name of this test target, as specified by the target's PRODUCT_MODULE_NAME build setting in Xcode.
 *
 *            SystemAttachmentLifetime <string>
 *            How long automatic UI testing screenshots should be kept. Should be one of the following string values:
 *
 *                  keepAlways
 *                  Always keep attachments, even for tests that succeed.
 *
 *                  deleteOnSuccess
 *                  Keep attachments for tests that fail, and discard them for tests that succeed.
 *
 *                  keepNever
 *                  Always discard attachments, regardless of whether the test succeeds or fails.
 *
 *            UserAttachmentLifetime <string>
 *            How long custom file attachments should be kept. Should be one of the string values specified in the SystemAttachmentLifetime
 *            section.
 *
 *            ParallelizationEnabled <bool>
 *            Whether or not the tests in this test target should be run in parallel using multiple test runner processes.
 *
 *            TestExecutionOrdering <string>
 *            The order in which tests should be run. By default, tests are run in alphabetical order and this field may be omitted, but
 *            tests may be run in a randomized order by specifying this setting with the string value "random".
 *
 *            TestLanguage <string>
 *            Language identifier code for the language which tests should be run using.
 *
 *            TestRegion <string>
 *            Region identifier code for the region which tests should be run using.
 *            
 *      The following are for advanced commands that control how xcodebuild installs test artifacts onto test destinations:
 *
 *            UseDestinationArtifacts <bool>
 *            An optional flag to indicate that xcodebuild should look on the destination for test artifacts. When this flag is set,
 *            xcodebuild will not install test artifacts to the destination during testing.  TestBundlePath, TestHostPath, and
 *            UITargetAppPath should be excluded when this flag is set. Instead, xcodebuild requires the following parameters.
 *
 *            TestHostBundleIdentifier <string>
 *            A bundle identifier for the test host on the destination. This parameter is mandatory when UseDestinationArtifacts is set.
 *
 *            TestBundleDestinationRelativePath <string>
 *            A path to the test bundle on the destination. This parameter is mandatory when UseDestinationArtifacts is set. The xcodebuild
 *            tool will expand the following placeholder strings in the path:
 *
 *                  __TESTHOST__
 *
 *            UITargetAppBundleIdentifier <string>
 *            A bundle identifier for the UI target application on the destination. This parameter is mandatory when UseDestinationArtifacts
 *            is set.
 *
 *      This last parameter is mandatory for all commands and is needed to configure the test host environment:
 *
 *            TestingEnvironmentVariables <dictionary of string keys and values>
 *            Additional testing environment variables that xcodebuild will provide to the TestHostPath process. The xcodebuild tool will
 *            expand the following placeholder strings in the dictionary values:
 *
 *                  __TESTBUNDLE__
 *                  __TESTHOST__
 *                  __TESTROOT__
 *                  __PLATFORMS__
 *                  __SHAREDFRAMEWORKS__
 *
 * CODE COVERAGE TARGETS SECTION
 *      The top-level CodeCoverageBuildableInfos array contains the list of targets for which code coverage information should be gathered
 *      while testing. Each entry is a dictionary containing metadata about the target. See the description of each field in the dictionary
 *      below.
 *
 *            Name <string>
 *            The name of the target's product, including any file extension. For example, "AppTests.xctest".
 *
 *            BuildableIdentifier <string>
 *            The buildable identifier of the target from the project, formatted as:
 *
 *                  <Target-Identifier>:<Buildable-Identifier>
 *
 *            For example, "123456ABCDEF:primary".
 *
 *            IncludeInReport <bool>
 *            Whether or not the target should be included in the code coverage report.
 *
 *            IsStatic <bool>
 *            Whether or not the target is a static archive library.
 *
 *            ProductPaths <array of string values>
 *            List of file paths to the variants of this target's build product. The xcodebuild tool will expand the following placeholder
 *            strings in the path:
 *
 *                  __TESTROOT__
 *
 *            Although each target for code coverage only has a single binary build product, this list may contain multiple entries because
 *            there may be multiple test configurations in the xctestrun file (per the top-level TestConfigurations array) and those
 *            configurations may have resulted in multiple build variants. Thus, each entry in this list represents a unique variant of the
 *            target's build product.
 *
 *            Architectures <array of string values>
 *            List of architectures for the variants of this target's build product.
 *
 *            Each architecture entry in this list describes the binary build product at the corresponding index of the ProductPaths array.
 *            There may be multiple entries in this list if the specified test configurations resulted in multiple build variants, see
 *            ProductPaths for more details.
 *
 *            SourceFiles <array of string values>
 *            List of file paths of the source files in the target whose code coverage should be measured. Any prefix which is common to all
 *            entries in this list should be removed from each entry and specified in the SourceFilesCommonPathPrefix field, so that the
 *            entries consist of only the portion of the file path after the common path prefix.
 *
 *            SourceFilesCommonPathPrefix <string>
 *            A file path prefix which all the source file entries in SourceFiles have in common. This prefix is applied to each entry in
 *            SourceFiles to determine the full path of each source file when generating the code coverage report.
 *
 *            Toolchains <array of string values>
 *            List of identifiers of Xcode toolchains to use when generating the code coverage report.
 *            
 * METADATA SECTION
 *      The top-level __xctestrun_metadata__ dictionary contains special metadata about the format of the xctestrun file. It currently
 *      contains only one field:
 *
 *            FormatVersion <number>
 *            The version of the xctestrun file format. Currently equal to 2. This must be specified in order for xcodebuild to interpret
 *            the xctestrun file correctly for the version indicated.
 *
 *      If the __xctestrun_metadata__ section is not present, xcodebuild will interpret the xctestrun file using an earlier format (version
 *      1).
 *
 * PLACEHOLDER STRINGS
 *      This section describes the various placeholder strings which may be used within certain settings' values in an xctestrun file. See
 *      the description of each setting to see which of these placeholders, if any, it supports.
 *
 *            __TESTBUNDLE__
 *            The path to the test bundle. This is expanded to a device path when UseDestinationArtifacts is set.
 *
 *            __TESTHOST__
 *            The test host directory provided by TestHostPath.
 *
 *            __TESTROOT__
 *            The directory containing the xctestrun file.
 *
 *            __PLATFORMS__
 *            The platforms directory in the active Xcode.app.
 *
 *            __SHAREDFRAMEWORKS__
 *            The shared frameworks directory in the active Xcode.app.
 */
class Xctestrun(delegate: NSDictionary): PropertyList<NSDictionary>(delegate) {
    constructor(metadata: Metadata, testConfigurations: Array<TestConfiguration>, testPlan: TestPlan? = null, codeCoverageBuildableInfos: Array<CodeCoverageBuildableInfo>? = null) : this(NSDictionary()) {
        this.metadata = metadata
        this.testConfigurations = testConfigurations
        testPlan?.let { this.testPlan = it }
        codeCoverageBuildableInfos?.let { this.codeCoverageBuildableInfos = it }
    }
    
    /**
     * The top-level __xctestrun_metadata__ dictionary contains special metadata about the format of the xctestrun file
     */
    var metadata: Metadata by delegate.plistDelegateFor("__xctestrun_metadata__", Metadata::class)

    /**
     * Contains metadata about the test plan which was used to construct this xctestrun file. 
     * It is provided for informational purposes and to allow distinguishing between xctestrun files 
     * if multiple were generated from a single scheme.
     * 
     * The keys are not used when performing tests.
     */
    var testPlan: TestPlan? by delegate.optionalPlistDelegateFor("TestPlan", TestPlan::class)

    /**
     * Contains the list of targets for which code coverage information should be gathered while testing.
     */
    var codeCoverageBuildableInfos: Array<CodeCoverageBuildableInfo> by delegate.plistListDelegateFor("CodeCoverageBuildableInfos", CodeCoverageBuildableInfo::class, true)

    /**
     * The top-level TestConfigurations array contains the list of test configurations to use when testing. 
     * Each entry is a dictionary containing metadata and a list of test targets to include.
     */
    var testConfigurations: Array<TestConfiguration> by delegate.plistListDelegateFor("TestConfigurations", TestConfiguration::class, false)
}
