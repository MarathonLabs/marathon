package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import com.malinskiy.marathon.generateTest
import org.amshove.kluent.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object TestNameRegexTestMatcherTestSpec : Spek({
   describe("fixed quota retry strategy tests") {
       val test1 = generateTest(pkg = "com.test", clazz = "SomeTest", method = "helloWorld")
       val test2 = test1.copy(clazz = "SomeExtraTest")

       it("test name is the same but classes are different") {
           val retryMatcher = TestNameRegexTestMatcher(pkg = null, clazz = "^SomeExtraTest$", method = "^helloWorld$")
           retryMatcher.matches(test2) shouldBe true
           retryMatcher.matches(test1) shouldBe false
       }
   }
})