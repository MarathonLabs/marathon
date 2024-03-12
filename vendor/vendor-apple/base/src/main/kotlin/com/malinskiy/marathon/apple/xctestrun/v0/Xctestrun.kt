package com.malinskiy.marathon.apple.xctestrun.v0

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.apple.plist.PropertyList

/**
 * Old version of xctestrun format without metadata
 * 
 * Man-page reference from April 11, 2016:
 * NAME
 *
 *      xcodebuild.xctestrun -- Test run parameters files for xcodebuild
 *
 *
 * DESCRIPTION
 *
 *      This document details the parameters contained in an xctestrun file. Dur-
 *      ing the build-for-testing action, xcodebuild extracts parameters from the
 *      scheme provided to the build-for-testing action and writes the parameters
 *      to an xctestrun file in the built products directory. A developer can
 *      edit the parameters in the xctestrun file and then provide the file to
 *      the xcodebuild test-without-building action with the -xctestrun option.
 *
 *
 * PROPERTY LIST KEYS
 *
 *      An xctestrun file contains discrete chunks of parameters - a chunk for
 *      each bundle that xcodebuild will test. Each chunk can contain the follow-
 *      ing parameters, and some of the parameters are mandatory, as described
 *      below.
 *
 *      The following parameters are mandatory during basic commands:
 *
 *            TestBundlePath <string>
 *            A path to the test bundle to be tested. The xcodebuild tool will
 *            expand the following placeholder strings in the path:
 *
 *                  __TESTROOT__
 *                  The directory containing the xctestrun file.
 *
 *                  __TESTHOST__
 *                  The test host directory bundle provided by TestHostPath.
 *
 *            TestHostPath <string>
 *            A path to the test host. For framework tests, this should be a path
 *            to the xctest command line tool. For application hosted tests, this
 *            should be a path the application host. For UI tests, this should be
 *            a path to the test runner application that the UI test target pro-
 *            duces. The xcodebuild tool will expand the following placeholder
 *            strings in the path:
 *
 *                  __TESTROOT__
 *                  The directory containing the xctestrun file.
 *
 *                  __PLATFORMS__
 *                  The platforms directory in the active Xcode.app.
 *
 *            UITargetAppPath <string>
 *            A path to the target application for UI tests. The parameter is
 *            mandatory for UI tests only. The xcodebuild tool will expand the
 *            following placeholder strings in the path:
 *
 *                  __TESTROOT__
 *                  The directory containing the xctestrun file.
 *
 *      These parameters are optional for all commands:
 *
 *            EnvironmentVariables <dictionary of string keys and values>
 *            The environment variables from the scheme test action that
 *            xcodebuild will provide to the test host process.
 *
 *            CommandLineArguments <array of string values>
 *            The command line arguments from the scheme test action that
 *            xcodebuild will provide to the test host process.
 *
 *            UITargetAppEnvironmentVariables <dictionary of string keys and
 *            values>
 *            The environment variables that xcodebuild will provide to the tar-
 *            get application during UI tests.
 *
 *            UITargetAppCommandLineArguments <array of string values>
 *            The command line arguments that xcodebuild will provide to the tar-
 *            get application during UI tests.
 *
 *            BaselinePath <string>
 *            A path to a performance test baseline that xcodebuild will provide
 *            to the tests. The xcodebuild tool will expand the following place-
 *            holder strings in the path:
 *
 *                  __TESTBUNDLE__
 *                  The path to the test bundle. This is expanded to a device
 *                  path when UseDestinationArtifacts is set.
 *
 *            SkipTestIdentifiers <array of strings>
 *            An array of test identifiers that xcodebuild should exclude
 *            from the test run.
 *
 *                  Test Identifier Format
 *                  Identifiers for both Swift and Objective-C tests are:
 *
 *                        Test-Class-Name[/Test-Method-Name]
 *
 *                  To exclude all the tests in a class Example.m, the
 *                  identifier is just "Example". To exclude one specific
 *                  test in the class, the identifier is "Example/testExam-
 *                  ple".
 *
 *            OnlyTestIdentifiers <array of strings>
 *            An array of test identifiers that xcodebuild should include
 *            in the test run. All other tests will be excluded from the
 *            test run. The format for the identifiers is described above.
 *
 *      The following are for advanced commands that control how xcodebuild
 *      installs test artifacts onto test destinations:
 *
 *            UseDestinationArtifacts <bool>
 *            An optional flag to indicate that xcodebuild should look on
 *            the destination for test artifacts. When this flag is set,
 *            xcodebuild will not install test artifacts to the destination
 *            during testing.  TestBundlePath, TestHostPath, and
 *            UITargetPath should be excluded when this flag is set.
 *            Instead, xcodebuild requires the following parameters.
 *
 *            TestHostBundleIdentifier <string>
 *            A bundle identifier for the test host on the destination.
 *            This parameter is mandatory when UseDestinationArtifacts is
 *            set.
 *
 *            TestBundleDestinationRelativePath <string>
 *            A path to the test bundle on the destination. This parameter
 *            is mandatory when UseDestinationArtifacts is set. The
 *            xcodebuild tool will expand the following placeholder strings
 *            in the path:
 *
 *                  __TESTHOST__
 *                  The test host directory bundle on the destination.
 *
 *            UITargetAppBundleIdentifier <string>
 *            A bundle identifier for the UI target application on the des-
 *            tination. This parameter is mandatory when
 *            UseDestinationArtifacts is set.
 *
 *      This last parameter is mandatory for all commands and is needed to
 *      configure the test host environment:
 *
 *            TestingEnvironmentVariables <dictionary of string keys and
 *            values>
 *            Additional testing environment variables that xcodebuild will
 *            provide to the TestHostPath process. The xcodebuild tool will
 *            expand the following placeholder strings in the dictionary
 *            values:
 *
 *                  __TESTBUNDLE__
 *                  The path to the test bundle. This is expanded to a
 *                  device path when UseDestinationArtifacts is set.
 *
 *                  __TESTHOST__
 *                  The test host directory provided by TestHostPath.
 *
 *                  __TESTROOT__
 *                  The directory containing the xctestrun file.
 *
 *                  __PLATFORMS__
 *                  The platforms directory in the active Xcode.app.
 *
 *                  __SHAREDFRAMEWORKS__
 *                  The shared frameworks directory in the active
 *                  Xcode.app.
 */
class Xctestrun(delegate: NSDictionary) : PropertyList<NSDictionary>(delegate) {
    constructor(targetName: String, target: TestTarget) : this(NSDictionary()) {
        delegate[targetName] = target.delegate
    }
    
    val targets: Map<String, TestTarget> by lazy {
        delegate.mapValues {
            TestTarget((delegate[it.key] as NSDictionary))
        }
    }
}

