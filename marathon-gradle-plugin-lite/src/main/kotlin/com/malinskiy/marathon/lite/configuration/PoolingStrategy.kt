package com.malinskiy.marathon.lite.configuration

import com.malinskiy.marathon.cliconfig.proto.PoolingStrategy.Abi
import com.malinskiy.marathon.cliconfig.proto.PoolingStrategy.Combo
import com.malinskiy.marathon.cliconfig.proto.PoolingStrategy.Manufacturer
import com.malinskiy.marathon.cliconfig.proto.PoolingStrategy.Model
import com.malinskiy.marathon.cliconfig.proto.PoolingStrategy.Omni
import com.malinskiy.marathon.cliconfig.proto.PoolingStrategy.OsVersion
import com.malinskiy.marathon.cliconfig.proto.PoolingStrategy.newBuilder
import java.io.Serializable

sealed class PoolingStrategy : Serializable {
    object Omni : PoolingStrategy()
    object ABI : PoolingStrategy()
    data class Combo(val list: List<PoolingStrategy>) : PoolingStrategy()
    object Manufacturer : PoolingStrategy()
    object Model : PoolingStrategy()
    object OsVersion : PoolingStrategy()
}

fun PoolingStrategy.toProto(): com.malinskiy.marathon.cliconfig.proto.PoolingStrategy {
    val builder = newBuilder()
    when (this) {
        PoolingStrategy.Omni -> {
            builder.omni = Omni.getDefaultInstance()
        }
        PoolingStrategy.ABI -> {
            builder.abi = Abi.getDefaultInstance()
        }
        PoolingStrategy.Manufacturer -> {
            builder.manufacturer = Manufacturer.getDefaultInstance()
        }
        PoolingStrategy.Model -> {
            builder.model = Model.getDefaultInstance()
        }
        PoolingStrategy.Manufacturer -> {
            builder.manufacturer = Manufacturer.getDefaultInstance()
        }
        PoolingStrategy.OsVersion -> {
            builder.osVersion = OsVersion.getDefaultInstance()
        }
        is PoolingStrategy.Combo -> {
            builder.combo = Combo.newBuilder().addAllList(list.map { it.toProto() }).build()
        }
    }
    return builder.build()
}
