package com.malinskiy.marathon.vendor.junit4.rule

import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.vendor.junit4.Junit4TestBundleIdentifier
import com.malinskiy.marathon.vendor.junit4.parsing.JupiterTestParser
import com.malinskiy.marathon.vendor.junit4.parsing.RemoteJupiterTestParser
import org.junit.rules.MethodRule
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

class ParserRule : MethodRule {
    private lateinit var internalTestParser: TestParser

    override fun apply(base: Statement?, method: FrameworkMethod?, target: Any?): Statement {
        return object : Statement() {
            override fun evaluate() {
                var bundleIdentifier = Junit4TestBundleIdentifier()
                internalTestParser = JupiterTestParser(bundleIdentifier)
                base?.evaluate()

                bundleIdentifier = Junit4TestBundleIdentifier()
                internalTestParser = RemoteJupiterTestParser(bundleIdentifier)
                base?.evaluate()
            }
        }
    }

    val testParser: TestParser
        get() = internalTestParser
}
