package com.malinskiy.marathon.vendor.junit4.booter.exec

import com.malinskiy.marathon.vendor.junit4.booter.contract.TestDescription
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestEvent
import com.malinskiy.marathon.vendor.junit4.booter.filter.TestFilter
import com.malinskiy.marathon.vendor.junit4.booter.isolation.ChildFirstURLClassLoader
import com.malinskiy.marathon.vendor.junit4.booter.server.ListenerFlowAdapter
import com.malinskiy.marathon.vendor.junit4.booter.server.switch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.junit.runner.Description
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import org.junit.runner.Result
import org.junit.runner.notification.RunListener
import java.io.File
import java.net.URLClassLoader

class InProcessTestExecutor : TestExecutor {
    private val core = JUnitCore()

    override fun run(
        tests: MutableList<TestDescription>,
        javaHome: String?,
        javaOptions: List<String>,
        classpathList: MutableList<String>,
        workdir: String
    ): Flow<TestEvent> {
        val classloader: URLClassLoader = ChildFirstURLClassLoader(
            classpathList.map { File(it).toURI().toURL() }.toTypedArray(),
            Thread.currentThread().contextClassLoader
        )

        return callbackFlow {
            val actualClassLocator = mutableMapOf<Description, String>()
            val callback: RunListener = ListenerFlowAdapter(this, actualClassLocator)

            addCallback(callback)

            classloader.use {
                it.switch {
                    exec(tests, actualClassLocator)
                }
            }

            awaitClose {
                removeCallback(callback)
            }
        }
    }

    override fun terminate() = Unit

    private fun addCallback(callback: RunListener) {
        core.addListener(callback)
    }

    private fun removeCallback(callback: RunListener) {
        core.removeListener(callback)
    }

    private fun exec(tests: List<TestDescription>, actualClassLocator: MutableMap<Description, String>): Result? {
        val klasses = mutableSetOf<Class<*>>()
        val testDescriptions = tests.map { test ->
            val fqtn = test.fqtn
            val klass = fqtn.substringBefore('#')
            val loadClass = Class.forName(klass)
            klasses.add(loadClass)

            val method = fqtn.substringAfter('#')
            Description.createTestDescription(loadClass, method)
        }.toHashSet()

        val testFilter = TestFilter(testDescriptions, actualClassLocator)
        val request = Request.classes(*klasses.toTypedArray())
            .filterWith(testFilter)

        return try {
            core.run(request)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
