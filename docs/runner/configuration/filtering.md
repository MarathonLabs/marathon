---
title: "Filtering"
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

Test filtering in marathon is done using **allowlist** and **blocklist** pattern.

:::tip

Test filtering works after a list of tests expected to run has been determined using [test class regular expression][1].
If your tests are not showing up after applying filtering logic - double-check the above parameter is not filtering it out.
:::

## Filtering logic

First allowlist is applied, then the blocklist. Each accepts a collection of **TestFilter** declarations and their parameters.

All the filters can be used in allowlist and in blocklist block as well, for example the following will run only smoke tests:

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
filteringConfiguration:
  allowlist:
    - type: "annotation"
      values:
        - "com.example.SmokeTest"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  filteringConfiguration {
    allowlist {
      add(AnnotationFilterConfiguration(values = listOf("com.example.SmokeTest")))
    }
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  filteringConfiguration {
    allowlist {
      annotationFilter = ['com.example.SmokeTest']
    }
  }
}
```

</TabItem>
</Tabs>

And the next snippet will execute everything, but the smoke tests:

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
filteringConfiguration:
  blocklist:
    - type: "annotation"
      values:
        - "com.example.SmokeTest"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  filteringConfiguration {
    blocklist {
      add(AnnotationFilterConfiguration(values = listOf("com.example.SmokeTest")))
    }
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  filteringConfiguration {
    blocklist {
      annotationFilter = ['com.example.SmokeTest']
    }
  }
}
```

</TabItem>
</Tabs>

### "fully-qualified-test-name"
Filters tests by their FQTN which is `$package.$class#$method`. The `#` sign is important!

### "fully-qualified-class-name"
Filters tests by their FQCN which is `$package.$class`                                     |

### "simple-test-name"
Filters tests by using only test class name with test method, e.g. `MyTest#mySimpleTest`

### "simple-class-name" 
Filters tests by using only test class name, e.g. `MyTest`

### "package"
Filters tests by using only test package, e.g. `com.example`

### "method"
Filters tests by using only test method, e.g. `myComplicatedTest`

### "annotation"
Filters tests by using only test annotation name, e.g. `androidx.test.filters.LargeTest`

### "allure"
Filters tests by using allure-test-filter, [source][2]

### Gradle plugin mapping

| YAML type                    |                 Gradle class                 |
|------------------------------|:--------------------------------------------:|
| "fully-qualified-test-name"  | `FullyQualifiedTestnameFilterConfiguration`  |
| "fully-qualified-class-name" | `FullyQualifiedClassnameFilterConfiguration` |
| "simple-test-name"           |     `SimpleTestnameFilterConfiguration`      |
| "simple-class-name"          |     `SimpleClassnameFilterConfiguration`     |
| "package"                    |       `TestPackageFilterConfiguration`       |
| "method"                     |       `TestMethodFilterConfiguration`        |
| "annotation"                 |       `AnnotationFilterConfiguration`        |
| "allure"                     |         `AllureFilterConfiguration`          |

:::caution

Gradle will require you to import the filtering classes just as any Groovy/Kotlin code would. 
Using Kotlin DSL will make it a bit simpler though.

:::

## Filter parameters

Each of the above filters expects **only one** of the following parameters:

- A `regex` for matching
- An array of `values`
- A `file` that contains each value on a separate line (empty lines will be ignored)

Each filter also has an `enabled` option (*default: `true`*). You can use this option to partially enable/disable filters.

### Regex filtering

An example of `regex` filtering is executing any test for a particular package, e.g. for package: `com.example` and it's subpackages:

```yaml
filteringConfiguration:
  allowlist:
    - type: "package"
      regex: "com\.example.*"
```

### Values filtering

You could also specify each package separately via values:

```yaml
filteringConfiguration:
  allowlist:
    - type: "package"
      values:
        - "com.example"
        - "com.example.subpackage"
```

or utilize the package's `subpackages` field to include all subpackages for each entry:
```yaml
filteringConfiguration:
  allowlist:
    - type: "package"
      subpackages: true
      values:
        - "com.example"
```

:::note

Including subpackages via `subpackages: true` works only when using values or file. It doesn't apply to configuration specified via regex.
This feature is only available in the CLI version of marathon.

:::

### Values file filtering

Or you can supply these packages via a file (be careful with the relative paths: they will be relative to the workdir of the process):

```yaml
filteringConfiguration:
  allowlist:
    - type: "package"
      file: "testing/myfilterfile"
```

Inside the `testing/myfilterfile` you should supply the values, each on a separate line:

```text
com.example
com.example.subpackage
```

You can leave comments via the **#** symbol at the beginning of the line or after the space in a value line. 
It can be helpful for example for the blocklist when you needed to specify the reason of skipping a test or just for commenting some values:

```text
#this is comment
com.example #skipped because of flakiness
com.example.subpackage
```

### Groovy filtering

With almost every filtering configuration (except for `annotationDataFilter`) it is possible to have `regex` and `values`.

Providing one value will be mapped to `regex` in the generated Marathonfile, more than one will end up in `values`

```groovy
marathon {
  filteringConfiguration {
    allowlist {
      annotationFilter = ['com.example.SmokeTest']//mapped to regex
      testMethodFilter = ['testMethod', 'testSomethingElse']//mapped to values
    }
  }
}
```

## Common examples

### Running only specific tests

A common scenario is to execute a list of tests. You can do this via the FQTN filter:

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
filteringConfiguration:
  allowlist:
    - type: "fully-qualified-test-name"
      values:
        - "com.example.ElaborateTest#testMethod"
        - "com.example.subpackage.OtherTest#testSomethingElse"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  filteringConfiguration {
    allowlist {
      add(
        FullyQualifiedTestnameFilterConfiguration(
          values = listOf(
            "com.example.ElaborateTest#testMethod",
            "com.example.subpackage.OtherTest#testSomethingElse",
          )
        )
      )
    }
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  filteringConfiguration {
    allowlist {
      fullyQualifiedTestnameFilter = ['com.example.ElaborateTest#testMethod', 'com.example.subpackage.OtherTest#testSomethingElse']
    }
  }
}
```

</TabItem>
</Tabs>

### Running only specific test classes

If you want to execute tests `ScaryTest` and `FlakyTest` for any package using the *class name* filter:

```yaml
filteringConfiguration:
  allowlist:
  - type: "simple-class-name"
    values:
      - "ScaryTest"
      - "FlakyTest" 
```

### Extracting filtering values into a separate file
In case you want to separate the filtering configuration from the *Marathonfile* you can supply a reference to an external file:

```yaml
filteringConfiguration:
  allowlist:
  - type: "simple-class-name"
    file: testing/myfilterfile
```

:::tip

This extraction approach works for any test filter that supports **values**.

:::

Inside the `testing/myfilterfile` you should supply the same values, each on a separate line, e.g. *fully qualified class name* filter:

```
com.example.ScaryTest
com.example.subpackage.FlakyTest
```

### Allure platform test filter
<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
filteringConfiguration:
  allowlist:
  - type: "allure"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  filteringConfiguration {
    allowlist {
      add(TestFilterConfiguration.AllureFilterConfiguration)
    }
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
marathon {
  filteringConfiguration {
    allowlist {
      allureTestFilter = true
    }
  }
}
```

</TabItem>
</Tabs>

### Composition filtering

Marathon supports filtering using multiple test filters at the same time using a *composition* filter. It accepts a list of base filters and
also an operation such as **UNION**, **INTERSECTION** or **SUBTRACT**. You can create complex filters such as get all the tests starting
with *E2E* but get only methods from there ending with *Test*. Composition filter is not supported by groovy gradle scripts, but is
supported if you use gradle kts.

An important thing to mention is that by default platform specific ignore options are not taken into account. This is because a
cross-platform test runner cannot account for all the possible test frameworks out there. However, each framework's ignore option can still
be "explained" to marathon, e.g. JUnit's **org.junit.Ignore** annotation can be specified in the filtering configuration.

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
filteringConfiguration:
  allowlist:
    - type: "simple-class-name"
      regex: ".*"
    - type: "fully-qualified-class-name"
      values:
        - "com.example.MyTest"
        - "com.example.MyOtherTest"
    - type: "fully-qualified-class-name"
      file: "testing/mytestfilter"
    - type: "method"
      regex: "."
    - type: "composition"
      filters:
        - type: "package"
          regex: ".*"
        - type: "method"
          regex: ".*"
      op: "UNION"
  blocklist:
    - type: "package"
      regex: ".*"
    - type: "annotation"
      regex: ".*"
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  filteringConfiguration {
    allowlist = mutableListOf(
      SimpleClassnameFilterConfiguration(".*".toRegex()),
      FullyQualifiedClassnameFilterConfiguration(".*".toRegex()),
      TestMethodFilterConfiguration(".*".toRegex()),
      CompositionFilterConfiguration(
        listOf(
          TestPackageFilterConfiguration(".*".toRegex()),
          TestMethodFilterConfiguration(".*".toRegex())
        ),
        CompositionFilterConfiguration.OPERATION.UNION
      )
    )
    blocklist = mutableListOf(
      TestPackageFilterConfiguration(".*".toRegex()),
      AnnotationFilterConfiguration(".*".toRegex())
    )
  }
}
```

</TabItem>
<TabItem value="groovy" label="Groovy DSL">

```groovy
//With Groovy configuration, specifying only one value per configuration will result in regex
marathon {
  filteringConfiguration {
    allowlist {
      simpleClassNameFilter = [".*"]
      fullyQualifiedClassnameFilter = [".*"]
      testMethodFilter = [".*"]
    }
    blocklist {
      testPackageFilter = [".*"]
      annotationFilter = [".*"]
    }
  }
}
```

</TabItem>
</Tabs>

### Dynamically disable parts of the filteringConfiguration
Each filter in `filteringConfiguration` has an `enabled` option (*default: `true`*). You can use this option to change filtering on-the-fly 
without duplicating the `Marathonfile`:

```yaml
filteringConfiguration:
  blocklist:
    - type: "annotation"
      enabled: ${MARATHON_EXCLUDE_LARGE_TESTS}
      values: 
        - "androidx.test.filters.LargeTest"
```

```shell-session
foo@bar:~$ MARATHON_EXCLUDE_LARGE_TESTS=false marathon
```


:::note

The default value of envvar is empty. When using envvars as parameters for `enabled` flag you have to specify each and every variable

:::


## Fragmented execution of tests

This is a test filter similar to sharded test execution that [AOSP provides][3].

It is intended to be used in situations where it is not possible to connect multiple execution devices to a single test run, e.g. CI setup
that can schedule parallel jobs each containing a single execution device. There are two parameters for using fragmentation:

* **count** - the number of overall fragments (e.g. 10 parallel execution)
* **index** - current execution index (in our case of 10 executions valid indexes are 0..9)

This is a dynamic programming technique, hence the results will be sub-optimal compared to connecting multiple devices to the same test run

<Tabs>
<TabItem value="YAML" label="Marathonfile">

```yaml
filteringConfiguration:
  allowlist:
    - type: "fragmentation"
      index: 0
      count: 10
```

</TabItem>
<TabItem value="kts" label="Kotlin DSL">

```kotlin
marathon {
  filteringConfiguration {
    allowlist = mutableListOf(
      FragmentationFilterConfiguration(index = 0, count = 10)
    )
  }
}
```

</TabItem>
</Tabs>

If you want to dynamically pass the index of the test run you can use yaml envvar interpolation, e.g.:

```yaml
filteringConfiguration:
  allowlist:
    - type: "fragmentation"
      index: ${MARATHON_FRAGMENT_INDEX}
      count: 10
```

and then execute the testing as following:

```bash
$ MARATHON_FRAGMENT_INDEX=0 marathon
```

To pass the fragment index in gradle refer to
the [Gradle's dynamic project properties](https://docs.gradle.org/current/javadoc/org/gradle/api/Project.html#properties)

[1]: ../intro/configure.md#test-class-regular-expression
[2]: https://github.com/allure-framework/allure-java/tree/master/allure-test-filter
[3]: https://source.android.com/devices/tech/test_infra/tradefed/architecture/advanced/sharding
