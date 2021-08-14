package com.malinskiy.marathon.vendor.junit4.booter.server

import com.google.protobuf.Empty
import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.vendor.junit4.parser.contract.DiscoverEvent
import com.malinskiy.marathon.vendor.junit4.parser.contract.DiscoverRequest
import com.malinskiy.marathon.vendor.junit4.parser.contract.EventType
import com.malinskiy.marathon.vendor.junit4.parser.contract.TestDescription
import com.malinskiy.marathon.vendor.junit4.parser.contract.TestParserGrpcKt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.platform.engine.discovery.ClassNameFilter
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.MethodSource
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import org.junit.platform.launcher.core.LauncherConfig
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.discovery.LauncherDiscoveryListeners
import org.junit.vintage.engine.VintageTestEngine
import java.io.File
import java.net.URLClassLoader
import kotlin.system.exitProcess

class TestParserService : TestParserGrpcKt.TestParserCoroutineImplBase() {
    override fun execute(request: DiscoverRequest): Flow<DiscoverEvent> = flow {
        val applicationClasspath = request.applicationClasspathList.map { File(it) }
        val testClasspath = request.testClasspathList.map { File(it) }

        val cp = mutableListOf<File>().apply {
            addAll(testClasspath)
            addAll(applicationClasspath)
        }.toList()

        val classpathToScan = cp.map { it.toURI().toURL() }
            .toTypedArray()
        val classloader: ClassLoader = URLClassLoader.newInstance(
            classpathToScan,
            Thread.currentThread().contextClassLoader
        )

        val plan: TestPlan = classloader.switch {
            val launcherConfig = LauncherConfig.builder()
                .addTestEngines(VintageTestEngine())
                .enableTestEngineAutoRegistration(false)
                .build()
            val launcher = LauncherFactory.create(launcherConfig)

            val discoveryRequest = LauncherDiscoveryRequestBuilder()
                .selectors(
                    DiscoverySelectors.selectPackage(request.rootPackage)
                )
                .listeners(LauncherDiscoveryListeners.logging())
                .filters(
                    ClassNameFilter.includeClassNamePatterns(ClassNameFilter.STANDARD_INCLUDE_PATTERN)
                )
                .build()

            launcher.discover(discoveryRequest)
        }

        if (plan.containsTests()) {
            plan.roots.forEach { root: TestIdentifier ->
                val tests = plan.getChildren(root)
                tests.forEach { test: TestIdentifier ->
                    when {
                        test.isContainer -> {
                            plan.getChildren(test).forEach { method ->
                                if (method.source.isPresent) {
                                    val source = method.source.get()
                                    when (source) {
                                        is MethodSource -> {
                                            emit(source.toTest().toEvent())
                                        }
                                        is ClassSource -> {
                                            val testIdentifier = plan.getTestIdentifier(method.uniqueId)
                                            if (plan.getParent(testIdentifier).isPresent) {
                                                val parent = plan.getParent(testIdentifier).get()
                                                val classSource = parent.source.get() as ClassSource
                                                emit(classSource.toTest(source, testIdentifier.displayName).toEvent())
                                            } else {
                                                println { "Unknown test ${method.uniqueId}" }
                                            }
                                        }
                                        else -> {
                                            println { "Unknown test ${method.uniqueId}" }
                                        }
                                    }
                                } else if (method.isContainer) {
                                    //Most likely a parameterized test
                                    plan.getChildren(method).forEach { parameterizedTest ->
                                        if (parameterizedTest.source.isPresent) {
                                            val source = parameterizedTest.source.get()
                                            when (source) {
                                                is MethodSource -> {
                                                    emit(source.toParameterizedTest(parameterizedTest).toEvent())
                                                }
                                                else -> {
                                                    println { "Unknown test ${parameterizedTest.uniqueId}" }
                                                }
                                            }
                                        } else {
                                            println { "Unknown test ${parameterizedTest.uniqueId}" }
                                        }
                                    }
                                } else {
                                    println { "Unknown test ${method.uniqueId}" }
                                }
                            }
                        }
                    }
                }
            }
        }

        emit(
            DiscoverEvent.newBuilder().setEventType(EventType.FINISHED).build()
        )
    }

    override suspend fun terminate(request: Empty): Empty {
        exitProcess(0)
        return super.terminate(request)
    }
}

private fun Test.toEvent(): DiscoverEvent {
    return DiscoverEvent.newBuilder()
        .setEventType(EventType.PARTIAL_PARSE)
        .addTest(toTestDescription())
        .build()
}

/**
 * Doesn't parse meta values yet
 */
private fun Test.toTestDescription(): TestDescription {
    return TestDescription.newBuilder()
        .setPkg(pkg)
        .setClazz(clazz)
        .setMethod(method)
        .addAllMetaProperties(metaProperties.map { it.name })
        .build()
}


private fun ClassSource.toTest(child: ClassSource, methodName: String): Test {
    val clazz = className.substringAfterLast(".")
    val pkg = className.substringBeforeLast(".")
    return Test(pkg, clazz, methodName, emptyList())
}

private fun MethodSource.toTest(): Test {
    val clazz = className.substringAfterLast(".")
    val pkg = className.substringBeforeLast(".")
    return Test(pkg, clazz, methodName, emptyList())
}

private fun MethodSource.toParameterizedTest(parameterizedTest: TestIdentifier): Test {
    val clazz = className.substringAfterLast(".")
    val pkg = className.substringBeforeLast(".")
    val meta = javaMethod.declaredAnnotations.mapNotNull { it.annotationClass.qualifiedName?.let { name -> MetaProperty(name) } } +
        javaClass.declaredAnnotations.mapNotNull { it.annotationClass.qualifiedName?.let { name -> MetaProperty(name) } }

    return Test(pkg, clazz, parameterizedTest.displayName, meta)
}
