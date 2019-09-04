package com.malinskiy.marathon.spek

import org.jetbrains.spek.api.dsl.TestBody
import org.koin.core.context.GlobalContext
import org.koin.core.definition.BeanDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.test.mock.declareMockedDefinition
import org.koin.test.mock.getDefinition

/**
 * Declare & Create a mock in Koin container for given type
 *
 * @author Arnaud Giuliani
 */
inline fun <reified T : Any> TestBody.declareMock(
    qualifier: Qualifier? = null,
    noinline stubbing: (T.() -> Unit)? = null
): T {
    val koin = GlobalContext.get().koin
    val clazz = T::class

    val foundDefinition: BeanDefinition<T> = getDefinition(clazz, koin, qualifier)

    koin.declareMockedDefinition(foundDefinition, stubbing)

    return koin.get()
}