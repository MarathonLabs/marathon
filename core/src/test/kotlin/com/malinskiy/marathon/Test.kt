package com.malinskiy.marathon

import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object CalculatorSpec: Spek({
    given("a calculator") {
        on("addition") {
            val sum = 2 + 4
            it("should return the result of adding the first number to the second number") {
                sum shouldEqual 6
            }
        }
        on("subtraction") {
            val subtract = 4 - 2
            it("should return the result of subtracting the second number from the first number") {
                subtract shouldEqual 2
            }
        }
    }
})
