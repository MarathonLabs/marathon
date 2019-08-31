package com.malinskiy.marathon.ios.xctestrun

typealias PropertyListMap = MutableMap<String, Any>

fun PropertyListMap.valueForKeypath(vararg keys: PropertyListKey): Any? =
    valueForKeypath(keypath = keys.map { it.toKeyString() }.joinToString(separator = Keypath.separator))

fun PropertyListMap.valueForKeypath(module: String, vararg keys: PropertyListKey): Any? {
    val keypath: Keypath = module + Keypath.separator + keys.map { it.toKeyString() }.joinToString(separator = Keypath.separator)
    return valueForKeypath(keypath = keypath)
}

class InvalidKeypathException(
    message: String = INVALID_KEYPATH_MESSAGE,
    val keypath: Keypath
) : RuntimeException(message) {
    companion object {
        const val INVALID_KEYPATH_MESSAGE = "Invalid keypath:"
    }

    override val message: String = "$INVALID_KEYPATH_MESSAGE: $keypath"
}

private typealias Keypath = String

private val String.Companion.separator: String
    get() = ":"

private fun Keypath.keypathRoot(): String = substringBefore(Keypath.separator)
private fun Keypath.keypathTail(): Keypath? = substringAfterOrNull(Keypath.separator)

private fun PropertyListMap.valueForKeypath(keypath: Keypath): Any? {
    return when {
        keypath.isEmpty() -> null
        else -> {
            val root = get(keypath.keypathRoot())
            val tail = keypath.keypathTail()
            when {
                tail == null || tail.isEmpty() -> root
                root is MutableMap<*, *> ->
                    try {
                        @Suppress("UNCHECKED_CAST")
                        (root as PropertyListMap).valueForKeypath(tail)
                    } catch (exception: InvalidKeypathException) {
                        throw InvalidKeypathException(keypath = keypath)
                    }
                else ->
                    throw InvalidKeypathException(keypath = keypath)
            }
        }
    }
}

private fun String.substringAfterOrNull(delimiter: String): String? {
    return when (contains(Keypath.separator)) {
        false -> null
        true -> substringAfter(delimiter)
    }
}
