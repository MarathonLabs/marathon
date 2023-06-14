package com.malinskiy.marathon.ios.logparser.formatter

object NoopPackageNameFormatter : PackageNameFormatter {
    override fun format(name: String?) = name
}
