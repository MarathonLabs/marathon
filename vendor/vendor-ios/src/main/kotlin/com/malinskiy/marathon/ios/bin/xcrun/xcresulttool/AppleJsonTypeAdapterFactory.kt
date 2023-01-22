package com.malinskiy.marathon.ios.bin.xcrun.xcresulttool

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.type.CollectionType


/**
 * For the Apple trickery with json we require a custom deserializer
 */
class AppleModule : SimpleModule() {
    init {
        setDeserializerModifier(object : BeanDeserializerModifier() {
            override fun modifyDeserializer(
                config: DeserializationConfig?,
                beanDesc: BeanDescription?,
                deserializer: com.fasterxml.jackson.databind.JsonDeserializer<*>?
            ): com.fasterxml.jackson.databind.JsonDeserializer<*> {
                return when (beanDesc?.beanClass) {
                    String::class.java, java.lang.String::class.java -> {
                        AppleJsonDeserializer(String::class.java, deserializer as com.fasterxml.jackson.databind.JsonDeserializer<String>)
                    }

                    java.util.Date::class.java -> AppleJsonDeserializer(
                        java.util.Date::class.java,
                        deserializer as com.fasterxml.jackson.databind.JsonDeserializer<java.util.Date>
                    )

                    Double::class.java, java.lang.Double::class.java -> {
                        AppleJsonDeserializer(Double::class.java, deserializer as com.fasterxml.jackson.databind.JsonDeserializer<Double>)
                    }

                    Int::class.java, java.lang.Integer::class.java -> {
                        AppleJsonDeserializer(Int::class.java, deserializer as com.fasterxml.jackson.databind.JsonDeserializer<Int>)
                    }

                    Boolean::class.java, java.lang.Boolean::class.java -> {
                        AppleJsonDeserializer(Boolean::class.java, deserializer as com.fasterxml.jackson.databind.JsonDeserializer<Boolean>)
                    }

                    else -> super.modifyDeserializer(config, beanDesc, deserializer)
                }
            }

            override fun modifyCollectionDeserializer(
                config: DeserializationConfig,
                type: CollectionType,
                beanDesc: BeanDescription,
                deserializer: com.fasterxml.jackson.databind.JsonDeserializer<*>
            ): com.fasterxml.jackson.databind.JsonDeserializer<*> {
                if (type.isTypeOrSubTypeOf(List::class.java) && deserializer::class.java.isAssignableFrom(CollectionDeserializer::class.java)) {
                    return AppleListJson(
                        type.contentType.rawClass,
                        type,
                    )
                }
                return super.modifyCollectionDeserializer(config, type, beanDesc, deserializer)
            }
        })
    }
}

class AppleJsonDeserializer<T>(
    clazz: Class<T>,
    private val deserializer: com.fasterxml.jackson.databind.JsonDeserializer<T>
) : StdNodeBasedDeserializer<T>(clazz) {
    override fun convert(root: JsonNode?, ctxt: DeserializationContext): T {
        val value = (root as? ObjectNode)?.get("_value")
        val nodeParser: JsonParser = value!!.traverse(ctxt.parser.codec)
        nodeParser.nextToken()
        return deserializer.deserialize(nodeParser, ctxt)
    }
}

class AppleListJson<T : Any>(
    private val valueClazz: Class<T>,
    listType: JavaType,
) : StdNodeBasedDeserializer<List<T>>(listType) {
    override fun convert(root: JsonNode?, ctxt: DeserializationContext): List<T> {
        val values = (root as? ObjectNode)?.get("_values") as? ArrayNode ?: return emptyList()
        return values.map {
            it.traverse(ctxt.parser.codec).readValueAs(valueClazz)
        }
    }
}
