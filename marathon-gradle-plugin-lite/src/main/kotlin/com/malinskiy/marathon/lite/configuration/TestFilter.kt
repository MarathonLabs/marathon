package com.malinskiy.marathon.lite.configuration

import com.malinskiy.marathon.cliconfig.proto.FilteringConfiguration.TestFilter.*
import com.malinskiy.marathon.cliconfig.proto.FilteringConfiguration.TestFilter.Annotation
import com.malinskiy.marathon.cliconfig.proto.FilteringConfiguration.TestFilter.Annotation.*
import java.io.Serializable
import kotlin.String
import com.malinskiy.marathon.cliconfig.proto.FilteringConfiguration as ProtoFilteringConfiguration

sealed class TestFilter : Serializable {
    data class SimpleClassname(val simpleClassname: String) : TestFilter()
    data class FullyQualifiedClassname(val fullyQualifiedClassname: String) : TestFilter()
    data class TestPackage(val `package`: String) : TestFilter()
    data class Annotation(val annotation: String) : TestFilter()
    data class TestMethod(val method: String) : TestFilter()
    data class Composition(val composition: List<TestFilter>, val op: OPERATION) : TestFilter()
    enum class OPERATION {
        UNION,
        INTERSECTION,
        SUBTRACT;
    }
}

fun TestFilter.toProto(): ProtoFilteringConfiguration.TestFilter {
    val builder = ProtoFilteringConfiguration.TestFilter.newBuilder()
    when (this) {
        is TestFilter.SimpleClassname -> {
            builder.simpleClassname = SimpleClassname.newBuilder().setSimpleClassname(simpleClassname).build()
        }
        is TestFilter.Annotation -> {
            builder.annotation = Annotation.newBuilder().setAnnotation(annotation).build()
        }
        is TestFilter.Composition -> {
            builder.composition =
                Composition.newBuilder().addAllFilters(composition.map { it.toProto() }).setOperation(op.toProto()).build()
        }
        is TestFilter.FullyQualifiedClassname -> {
            builder.fullyQualifiedClassname =
                FullyQualifiedClassname.newBuilder().setFullyQualifiedClassname(fullyQualifiedClassname).build()
        }
        is TestFilter.TestMethod -> {
            builder.testMethod = TestMethod.newBuilder().setMethod(method).build()
        }
        is TestFilter.TestPackage -> {
            builder.testPackage = TestPackage.newBuilder().setPackage(`package`).build()
        }
    }
    return builder.build()
}

fun TestFilter.OPERATION.toProto(): Composition.Operation {
    return when (this) {
        TestFilter.OPERATION.UNION -> Composition.Operation.UNION
        TestFilter.OPERATION.SUBTRACT -> Composition.Operation.SUBTRACT
        TestFilter.OPERATION.INTERSECTION -> Composition.Operation.INTERSECTION
    }
}
