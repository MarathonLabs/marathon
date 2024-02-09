package com.malinskiy.marathon.apple.ios.plist

import com.dd.plist.NSArray
import com.dd.plist.NSDictionary
import com.dd.plist.NSObject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor


/**
 * Minimally-invasive read and write for Apple plists:
 * I couldn't find any specification for all of xctestrun variations: man contains only V2
 *
 * Other problems include mentions of version 2 and version 1, but one can find examples of
 * xctestrun plists without any __xctestrun_metadata__, with the format of v2 but specifying v1
 *
 * This means handling such plists should be need-to-read/write only and shouldn't modify any values
 * that we don't know about
 */
fun <T> NSDictionary.delegateFor(name: String): ReadWriteProperty<Any, T> {
    return object : ReadWriteProperty<Any, T> {
        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            val someProperty =
                this@delegateFor[name]?.toJavaObject() ?: throw IllegalArgumentException("plist does not define property $name")
            return someProperty as? T ?: throw IllegalArgumentException("plist property $name is of type ${someProperty::class.java}")
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            this@delegateFor[name] = NSObject.fromJavaObject(value)
        }
    }
}

fun <T> NSDictionary.optionalDelegateFor(name: String): ReadWriteProperty<Any, T?> {
    return object : ReadWriteProperty<Any, T?> {
        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: Any, property: KProperty<*>): T? {
            val someProperty =
                this@optionalDelegateFor[name]?.toJavaObject() ?: return null
            return someProperty as? T ?: throw IllegalArgumentException("plist property $name is of type ${someProperty::class.java}")
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
            this@optionalDelegateFor[name] = NSObject.fromJavaObject(value)
        }
    }
}

inline fun <reified T> NSDictionary.optionalArrayDelegateFor(name: String) = arrayDelegateFor<T>(name, true)

/**
 * The dd plist deserializes NSArray into Object[]. This is not ideal, but generic delegate above will not work for
 * conversion into List<T>
 */
inline fun <reified T> NSDictionary.arrayDelegateFor(name: String, optional: Boolean): ReadWriteProperty<Any, Array<T>> {
    return object : ReadWriteProperty<Any, Array<T>> {
        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: Any, property: KProperty<*>): Array<T> {
            val someProperty = this@arrayDelegateFor[name]
                ?: if (optional) {
                    return emptyArray()
                } else {
                    throw IllegalArgumentException("plist does not define property $name")
                }
            
            val nsArray: NSArray =
                someProperty as? NSArray ?: throw IllegalArgumentException("plist property $name is of type ${someProperty::class.java}")
            
            return nsArray.array.map { 
                it.toJavaObject(T::class.java) 
            }.toTypedArray()
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Array<T>) {
            this@arrayDelegateFor[name] = NSObject.fromJavaObject(value)
        }
    }
}

fun <S : NSObject, T : PropertyList<S>> NSDictionary.plistDelegateFor(name: String, clazz: KClass<T>): ReadWriteProperty<Any, T> {
    return object : ReadWriteProperty<Any, T> {
        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            val someProperty = this@plistDelegateFor[name] ?: throw IllegalArgumentException("plist does not define property $name")
            return clazz.primaryConstructor?.call(someProperty)
                ?: throw IllegalArgumentException("type ${clazz.qualifiedName} should define primary constructor with a single parameter of type NSObject")
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            this@plistDelegateFor[name] = value.delegate
        }
    }
}

/**
 * We need to use typed arrays, so reified generic
 */
inline fun <reified S : NSObject, reified T : PropertyList<S>> NSDictionary.plistListDelegateFor(
    name: String,
    clazz: KClass<T>,
    optional: Boolean
): ReadWriteProperty<Any, Array<T>> {
    return object : ReadWriteProperty<Any, Array<T>> {
        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: Any, property: KProperty<*>): Array<T> {
            val someProperty = this@plistListDelegateFor[name]?.toJavaObject()
                ?: if (optional) {
                    return emptyArray()
                } else {
                    throw IllegalArgumentException("plist does not define property $name")
                }
            val array =
                someProperty as? Array<T> ?: throw IllegalArgumentException("plist property $name is of type ${someProperty::class.java}")
            return array.mapNotNull { clazz.primaryConstructor?.call(it) }.toTypedArray()
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Array<T>) {
            this@plistListDelegateFor[name] = NSArray.fromJavaObject(value.map { it.delegate}.toTypedArray())
        }
    }
}

fun <S : NSObject, T : PropertyList<S>> NSDictionary.optionalPlistDelegateFor(
    name: String,
    clazz: KClass<T>
): ReadWriteProperty<Any, T?> {
    return object : ReadWriteProperty<Any, T?> {
        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: Any, property: KProperty<*>): T? {
            val someProperty = this@optionalPlistDelegateFor[name] ?: return null
            return clazz.primaryConstructor?.call(someProperty)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
            value?.let {
                this@optionalPlistDelegateFor[name] = it.delegate
            }
        }
    }
}
