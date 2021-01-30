package com.malinskiy.marathon.cli.args

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.decoder.DataClassDecoder
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.invalid
import kotlin.reflect.KType
import kotlin.reflect.full.createType

interface FileVendorConfiguration {
}

class VendorDecoder : Decoder<FileVendorConfiguration> {
    override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<FileVendorConfiguration> {
        return when (node) {
            is MapNode -> {
                decodeType(node, type, context)
            }
            else -> ConfigFailure.DecodeError(node, type).invalid()
        }
    }

    private fun decodeType(node: MapNode, type: KType, context: DecoderContext): ConfigResult<FileVendorConfiguration> {
        val typeNode = node["type"]
        return when (typeNode) {
            is StringNode -> {
                createByType(typeNode.value, node, type, context);
            }
            else -> ConfigFailure.DecodeError(node, type).invalid()
        }
    }

    private fun createByType(vendorType: String, node: MapNode, type: KType, context: DecoderContext): ConfigResult<FileVendorConfiguration> {
        val configType = when(vendorType.toLowerCase()){
            "android" -> FileAndroidConfiguration::class.createType()
            "ios" -> FileIOSConfiguration::class.createType()
            else -> return ConfigFailure.DecodeError(node, type).invalid()
        }
        return DataClassDecoder().decode(node, configType, context).map { it as FileVendorConfiguration }
    }

    override fun supports(type: KType): Boolean {
        return type.classifier == FileVendorConfiguration::class
    }

}
