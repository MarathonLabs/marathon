package com.malinskiy.marathon.vendor.junit4.booter.filter

import org.junit.runner.Description
import org.junit.runner.manipulation.Filter

class TestFilter(
    private val testDescriptions: HashSet<Description>,
    private val actualClassLocator: MutableMap<Description, String>
) : Filter() {
    private val verifiedChildren = mutableSetOf<Description>()

    override fun shouldRun(description: Description): Boolean {
        println("JUnit asks about $description")

        return if (verifiedChildren.contains(description)) {
//            println("Already unfiltered $description before")
            true
        } else {
            shouldRun(description, className = null)
        }
    }

    fun shouldRun(description: Description, className: String?): Boolean {
        if (description.isTest) {
            println("$description")
            /**
             * Handling for parameterized tests that report org.junit.runners.model.TestClass as their test class
             */
            val verificationDescription = if (description.className == CLASS_NAME_STUB && className != null) {
                Description.createTestDescription(className, description.methodName, *description.annotations.toTypedArray())
            } else {
                description
            }
            val contains = testDescriptions.contains(verificationDescription)
            if (contains) {
                verifiedChildren.add(description)
                if (description.className == CLASS_NAME_STUB && className != null) {
                    actualClassLocator[description] = className
                }
            }

            return contains
        }

        // explicitly check if any children want to run
        var childrenResult = false
        for (each in description.children) {
//            println("$description")
            if (shouldRun(each!!, description.className)) {
                childrenResult = true
            }
        }

        return childrenResult
    }

    override fun describe() = "Marathon JUnit4 execution filter"

    companion object {
        const val CLASS_NAME_STUB = "org.junit.runners.model.TestClass"
    }
}
