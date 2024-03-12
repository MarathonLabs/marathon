package com.malinskiy.marathon.apple.xctestrun.v2

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.apple.plist.PropertyList
import com.malinskiy.marathon.apple.plist.delegateFor
import com.malinskiy.marathon.apple.plist.optionalArrayDelegateFor

/**
 * Note: unclear if all of the fields are mandatory or optional
 */
class CodeCoverageBuildableInfo(delegate: NSDictionary) : PropertyList<NSDictionary>(delegate) {
    constructor(
        name: String,
        buildableIdentifier: String,
        includeInReport: Boolean,
        isStatic: Boolean,
        productPaths: Array<String>,
        architectures: Array<String>,
        sourceFiles: Array<String>,
        sourceFilesCommonPathPrefix: String,
        toolchains: Array<String>,
    ) : this(
        NSDictionary()
    ) {
        this.name = name
        this.buildableIdentifier = buildableIdentifier
        this.includeInReport = includeInReport
        this.isStatic = isStatic
        this.productPaths = productPaths
        this.architectures = architectures
        this.sourceFiles = sourceFiles
        this.sourceFilesCommonPathPrefix = sourceFilesCommonPathPrefix
        this.toolchains = toolchains
    }

    /**
     * The name of the target's product, including any file extension. For example, "AppTests.xctest".
     */
    var name: String by delegate.delegateFor("Name")

    /**
     * The buildable identifier of the target from the project, formatted as:
     *
     *      <Target-Identifier>:<Buildable-Identifier>
     *
     * For example, "123456ABCDEF:primary".
     */
    var buildableIdentifier: String by delegate.delegateFor("BuildableIdentifier")

    /**
     * Whether or not the target should be included in the code coverage report.
     */
    var includeInReport: Boolean by delegate.delegateFor("IncludeInReport")

    /**
     * Whether or not the target is a static archive library.
     */
    var isStatic: Boolean by delegate.delegateFor("IsStatic")

    /**
     * List of file paths to the variants of this target's build product.
     * The xcodebuild tool will expand the following placeholder strings in the path:
     *
     * `__TESTROOT__`
     *
     * Although each target for code coverage only has a single binary build product, this list may contain multiple entries because
     * there may be multiple test configurations in the xctestrun file (per the top-level TestConfigurations array) and those
     * configurations may have resulted in multiple build variants. Thus, each entry in this list represents a unique variant of the
     * target's build product.
     */
    var productPaths: Array<String> by delegate.optionalArrayDelegateFor("ProductPaths")

    /**
     * List of architectures for the variants of this target's build product.
     *
     * Each architecture entry in this list describes the binary build product at the corresponding index of the [productPaths] array.
     * There may be multiple entries in this list if the specified test configurations resulted in multiple build variants, see
     * [productPaths] for more details.
     */
    var architectures: Array<String> by delegate.optionalArrayDelegateFor("Architectures")

    /**
     * List of file paths of the source files in the target whose code coverage should be measured. Any prefix which is common to all
     * entries in this list should be removed from each entry and specified in the [sourceFilesCommonPathPrefix] field, so that the
     * entries consist of only the portion of the file path after the common path prefix.
     */
    var sourceFiles: Array<String> by delegate.optionalArrayDelegateFor("SourceFiles")

    /**
     *  A file path prefix which all the source file entries in [sourceFiles] have in common. This prefix is applied to each entry in
     *  [sourceFiles] to determine the full path of each source file when generating the code coverage report.
     */
    var sourceFilesCommonPathPrefix: String by delegate.delegateFor("SourceFilesCommonPathPrefix")

    /**
     * List of identifiers of Xcode toolchains to use when generating the code coverage report.
     */
    var toolchains: Array<String> by delegate.optionalArrayDelegateFor("Toolchains")
}
