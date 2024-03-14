package com.malinskiy.marathon.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import java.io.File

data class FilteringConfiguration(
    @JsonProperty("allowlist", required = false) val allowlist: Collection<TestFilterConfiguration> = emptyList(),
    @JsonProperty("blocklist", required = false) val blocklist: Collection<TestFilterConfiguration> = emptyList()
)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TestFilterConfiguration.AnnotationDataFilterConfiguration::class, name = "annotationData"),
    JsonSubTypes.Type(value = TestFilterConfiguration.AnnotationFilterConfiguration::class, name = "annotation"),
    JsonSubTypes.Type(value = TestFilterConfiguration.CompositionFilterConfiguration::class, name = "composition"),
    JsonSubTypes.Type(value = TestFilterConfiguration.FragmentationFilterConfiguration::class, name = "fragmentation"),
    JsonSubTypes.Type(
        value = TestFilterConfiguration.FullyQualifiedClassnameFilterConfiguration::class,
        name = "fully-qualified-class-name"
    ),
    JsonSubTypes.Type(value = TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration::class, name = "fully-qualified-test-name"),
    JsonSubTypes.Type(value = TestFilterConfiguration.SimpleTestnameFilterConfiguration::class, name = "simple-test-name"),
    JsonSubTypes.Type(value = TestFilterConfiguration.SimpleClassnameFilterConfiguration::class, name = "simple-class-name"),
    JsonSubTypes.Type(value = TestFilterConfiguration.TestMethodFilterConfiguration::class, name = "method"),
    JsonSubTypes.Type(value = TestFilterConfiguration.TestPackageFilterConfiguration::class, name = "package"),
    JsonSubTypes.Type(value = TestFilterConfiguration.AllureFilterConfiguration::class, name = "allure"),
)
sealed class TestFilterConfiguration {
    abstract fun validate()

    data class SimpleClassnameFilterConfiguration(
        @JsonProperty("regex") val regex: Regex? = null,
        @JsonProperty("values") val values: List<String>? = null,
        @JsonProperty("file") val file: File? = null,
    ) : TestFilterConfiguration() {
        override fun validate() {
            var i = 0
            if (regex != null) i++
            if (values != null) i++
            if (file != null) i++

            if (i > 1) throw ConfigurationException("Only one of [regex,values,file] can be specified for ${this::class.simpleName}")
            if (i == 0) throw ConfigurationException("At least one of [regex,values,file] should be specified for ${this::class.simpleName}")
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SimpleClassnameFilterConfiguration

            if (!regex.toString().contentEquals(other.regex.toString())) return false
            if (values != other.values) return false
            if (file != other.file) return false

            return true
        }

        override fun hashCode(): Int {
            var result = regex?.hashCode() ?: 0
            result = 31 * result + (values?.hashCode() ?: 0)
            result = 31 * result + (file?.hashCode() ?: 0)
            return result
        }
    }

    data class SimpleTestnameFilterConfiguration(
        @JsonProperty("regex") val regex: Regex? = null,
        @JsonProperty("values") val values: List<String>? = null,
        @JsonProperty("file") val file: File? = null,
    ) : TestFilterConfiguration() {
        override fun validate() {
            var i = 0
            if (regex != null) i++
            if (values != null) i++
            if (file != null) i++

            if (i > 1) throw ConfigurationException("Only one of [regex,values,file] can be specified for ${this::class.simpleName}")
            if (i == 0) throw ConfigurationException("At least one of [regex,values,file] should be specified for ${this::class.simpleName}")
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SimpleClassnameFilterConfiguration

            if (!regex.toString().contentEquals(other.regex.toString())) return false
            if (values != other.values) return false
            if (file != other.file) return false

            return true
        }

        override fun hashCode(): Int {
            var result = regex?.hashCode() ?: 0
            result = 31 * result + (values?.hashCode() ?: 0)
            result = 31 * result + (file?.hashCode() ?: 0)
            return result
        }
    }

    data class FullyQualifiedClassnameFilterConfiguration(
        @JsonProperty("regex") val regex: Regex? = null,
        @JsonProperty("values") val values: List<String>? = null,
        @JsonProperty("file") val file: File? = null,
    ) : TestFilterConfiguration() {
        override fun validate() {
            var i = 0
            if (regex != null) i++
            if (values != null) i++
            if (file != null) i++

            if (i > 1) throw ConfigurationException("Only one of [regex,values,file] can be specified for ${this::class.simpleName}")
            if (i == 0) throw ConfigurationException("At least one of [regex,values,file] should be specified for ${this::class.simpleName}")
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FullyQualifiedClassnameFilterConfiguration

            if (!regex.toString().contentEquals(other.regex.toString())) return false
            if (values != other.values) return false
            if (file != other.file) return false

            return true
        }

        override fun hashCode(): Int {
            var result = regex?.hashCode() ?: 0
            result = 31 * result + (values?.hashCode() ?: 0)
            result = 31 * result + (file?.hashCode() ?: 0)
            return result
        }
    }

    data class TestPackageFilterConfiguration(
        @JsonProperty("regex") val regex: Regex? = null,
        @JsonProperty("values") val values: List<String>? = null,
        @JsonProperty("file") val file: File? = null,
    ) : TestFilterConfiguration() {
        override fun validate() {
            var i = 0
            if (regex != null) i++
            if (values != null) i++
            if (file != null) i++

            if (i > 1) throw ConfigurationException("Only one of [regex,values,file] can be specified for ${this::class.simpleName}")
            if (i == 0) throw ConfigurationException("At least one of [regex,values,file] should be specified for ${this::class.simpleName}")
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TestPackageFilterConfiguration

            if (!regex.toString().contentEquals(other.regex.toString())) return false
            if (values != other.values) return false
            if (file != other.file) return false

            return true
        }

        override fun hashCode(): Int {
            var result = regex?.hashCode() ?: 0
            result = 31 * result + (values?.hashCode() ?: 0)
            result = 31 * result + (file?.hashCode() ?: 0)
            return result
        }
    }

    data class AnnotationDataFilterConfiguration(
        @JsonProperty("nameRegex") val nameRegex: Regex,
        @JsonProperty("valueRegex") val valueRegex: Regex
    ) : TestFilterConfiguration() {
        override fun validate() {}

        override fun equals(other: Any?): Boolean {
            if (other !is AnnotationDataFilterConfiguration) return false
            return (nameRegex.toString() + valueRegex.toString()).contentEquals((other.nameRegex.toString() + other.valueRegex.toString()))
        }

        override fun hashCode(): Int = nameRegex.hashCode() + valueRegex.hashCode()
    }

    data class FullyQualifiedTestnameFilterConfiguration(
        @JsonProperty("regex") val regex: Regex? = null,
        @JsonProperty("values") val values: List<String>? = null,
        @JsonProperty("file") val file: File? = null,
    ) : TestFilterConfiguration() {
        override fun validate() {
            var i = 0
            if (regex != null) i++
            if (values != null) i++
            if (file != null) i++

            if (i > 1) throw ConfigurationException("Only one of [regex,values,file] can be specified for ${this::class.simpleName}")
            if (i == 0) throw ConfigurationException("At least one of [regex,values,file] should be specified for ${this::class.simpleName}")
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FullyQualifiedTestnameFilterConfiguration

            if (!regex.toString().contentEquals(other.regex.toString())) return false
            if (values != other.values) return false
            if (file != other.file) return false

            return true
        }

        override fun hashCode(): Int {
            var result = regex?.hashCode() ?: 0
            result = 31 * result + (values?.hashCode() ?: 0)
            result = 31 * result + (file?.hashCode() ?: 0)
            return result
        }
    }

    data class FragmentationFilterConfiguration(
        val index: Int,
        val count: Int,
    ) : TestFilterConfiguration() {
        override fun validate() {
            if (index < 0) throw ConfigurationException("Fragment index [$index] should be >= 0")
            if (count < 0) throw ConfigurationException("Fragment count [$count] should be >= 0")
            if (index >= count) throw ConfigurationException("Fragment index [$index] should be less than count [$count]")
        }
    }

    data class TestMethodFilterConfiguration(
        @JsonProperty("regex") val regex: Regex? = null,
        @JsonProperty("values") val values: List<String>? = null,
        @JsonProperty("file") val file: File? = null,
    ) : TestFilterConfiguration() {
        override fun validate() {
            var i = 0
            if (regex != null) i++
            if (values != null) i++
            if (file != null) i++

            if (i > 1) throw ConfigurationException("Only one of [regex,values,file] can be specified for ${this::class.simpleName}")
            if (i == 0) throw ConfigurationException("At least one of [regex,values,file] should be specified for ${this::class.simpleName}")
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TestMethodFilterConfiguration

            if (!regex.toString().contentEquals(other.regex.toString())) return false
            if (values != other.values) return false
            if (file != other.file) return false

            return true
        }

        override fun hashCode(): Int {
            var result = regex?.hashCode() ?: 0
            result = 31 * result + (values?.hashCode() ?: 0)
            result = 31 * result + (file?.hashCode() ?: 0)
            return result
        }
    }

    data class AnnotationFilterConfiguration(
        @JsonProperty("regex") val regex: Regex? = null,
        @JsonProperty("values") val values: List<String>? = null,
        @JsonProperty("file") val file: File? = null,
    ) : TestFilterConfiguration() {
        override fun validate() {
            var i = 0
            if (regex != null) i++
            if (values != null) i++
            if (file != null) i++

            if (i > 1) throw ConfigurationException("Only one of [regex,values,file] can be specified for ${this::class.simpleName}")
            if (i == 0) throw ConfigurationException("At least one of [regex,values,file] should be specified for ${this::class.simpleName}")
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AnnotationFilterConfiguration

            if (!regex.toString().contentEquals(other.regex.toString())) return false
            if (values != other.values) return false
            if (file != other.file) return false

            return true
        }

        override fun hashCode(): Int {
            var result = regex?.hashCode() ?: 0
            result = 31 * result + (values?.hashCode() ?: 0)
            result = 31 * result + (file?.hashCode() ?: 0)
            return result
        }
    }

    data class CompositionFilterConfiguration(
        @JsonProperty("filters") val filters: List<TestFilterConfiguration>,
        @JsonProperty("op") val op: OPERATION
    ) : TestFilterConfiguration() {
        override fun validate() {
            filters.forEach { it.validate() }
        }

        enum class OPERATION {
            UNION,
            INTERSECTION,
            SUBTRACT
        }

        override fun equals(other: Any?): Boolean {
            if (other !is CompositionFilterConfiguration) return false
            if (filters.count() != other.filters.count()) return false
            if (op != other.op) return false
            filters.forEach {
                if (!other.filters.contains(it)) return false
            }
            return true
        }

        override fun hashCode(): Int = filters.hashCode() + op.hashCode()
    }

    object AllureFilterConfiguration : TestFilterConfiguration() {
        override fun validate() {
        }

        override fun equals(other: Any?): Boolean {
            return super.equals(other)
        }

        override fun hashCode(): Int {
            return super.hashCode()
        }
    }
}
