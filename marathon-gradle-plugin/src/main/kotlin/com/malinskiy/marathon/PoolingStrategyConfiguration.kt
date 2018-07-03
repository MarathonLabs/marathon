package com.malinskiy.marathon

import com.malinskiy.marathon.execution.strategy.PoolingStrategy

class PoolingStrategyConfiguration {


    var default = false

}

fun PoolingStrategyConfiguration.toStrategy(): PoolingStrategy {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}