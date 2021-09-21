package com.malinskiy.marathon.vendor.junit4.integrationtests

import com.malinskiy.marathon.vendor.junit4.integrationtests.custom.Functional
import org.junit.runner.RunWith

@RunWith(Functional::class)
@Functional.Properties("custom-parameterized")
class CustomParameterizedTest {
}
