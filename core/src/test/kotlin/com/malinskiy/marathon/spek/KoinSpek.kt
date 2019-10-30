package com.malinskiy.marathon.spek

import com.malinskiy.marathon.di.analyticsModule
import org.jetbrains.spek.api.dsl.Spec
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

fun Spec.getKoin(): Koin = GlobalContext.get().koin

fun Spec.initKoin() {
    beforeEachTest {
        startKoin {
            modules(analyticsModule)
        }
    }

    afterEachTest {
        stopKoin()
    }
}

inline fun <reified T> Spec.get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T =
    getKoin().get(qualifier, parameters)

inline fun <reified T> Spec.inject(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> =
    getKoin().inject(qualifier, parameters)

inline fun <reified S, reified P> Spec.bind(
    noinline parameters: ParametersDefinition? = null
): S =
    getKoin().bind<S, P>(parameters)