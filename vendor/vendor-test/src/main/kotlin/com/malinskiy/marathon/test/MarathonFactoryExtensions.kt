package com.malinskiy.marathon.test

import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.test.factory.MarathonFactory

fun setupMarathon(f: MarathonFactory.() -> Unit): Marathon {
    val marathonFactory = MarathonFactory()
    return marathonFactory.apply(f).build()
}
