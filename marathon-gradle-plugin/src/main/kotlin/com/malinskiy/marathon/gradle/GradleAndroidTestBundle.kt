package com.malinskiy.marathon.gradle

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.TestVariant

data class GradleAndroidTestBundle(
    val application: BaseVariant,
    val testApplication: TestVariant,
)
