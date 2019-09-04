package com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized

import com.malinskiy.marathon.device.DeviceStub
import org.amshove.kluent.shouldBeEqualTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class ComboPoolingStrategySpek : Spek(
    {
        describe("combo pooling strategy tests") {
            group("one strategy") {
                it("should return DevicePoolId name equals to DevicePoolId name from base strategy") {
                    val baseStrategy = AbiPoolingStrategy()
                    val comboPoolingStrategy = ComboPoolingStrategy(listOf(baseStrategy))
                    val abi = "x64"
                    val device = DeviceStub(abi = abi)
                    comboPoolingStrategy.associate(device).name shouldBeEqualTo abi
                }
            }
            group("two strategies") {
                it("should return DevicePoolId name equals DevicePoolId0.name_DevicePoolId1.name") {
                    val modelStrategy = ModelPoolingStrategy()
                    val abiStrategy = AbiPoolingStrategy()
                    val comboPoolingStrategy = ComboPoolingStrategy(listOf(modelStrategy, abiStrategy))
                    val abi = "x64"
                    val model = "TestDeviceModel"
                    val device = DeviceStub(abi = abi, model = model)
                    comboPoolingStrategy.associate(device).name shouldBeEqualTo "${model}_$abi"
                }
            }
            group("three strategies") {
                it("should return DevicePoolId name equals DevicePoolId0.name_DevicePoolId1.name_DevicePoolId2.name") {
                    val manufacturerStrategy = ManufacturerPoolingStrategy()
                    val modelStrategy = ModelPoolingStrategy()
                    val abiStrategy = AbiPoolingStrategy()
                    val comboPoolingStrategy =
                        ComboPoolingStrategy(listOf(manufacturerStrategy, modelStrategy, abiStrategy))
                    val manufacturer = "TestDeviceManufacturer"
                    val abi = "x64"
                    val model = "TestDeviceModel"
                    val device = DeviceStub(abi = abi, model = model, manufacturer = manufacturer)
                    comboPoolingStrategy.associate(device).name shouldBeEqualTo "${manufacturer}_${model}_$abi"
                }
            }
        }
    })
