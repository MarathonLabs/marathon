package com.malinskiy.marathon

import com.malinskiy.marathon.execution.strategy.PoolingStrategy as RealPoolingStrategy

class PoolingStrategy {


    var default = false

}

fun PoolingStrategy.toRealStrategy(): RealPoolingStrategy {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}