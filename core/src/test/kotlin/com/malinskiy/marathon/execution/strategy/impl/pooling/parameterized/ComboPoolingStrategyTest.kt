package com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized

import com.malinskiy.marathon.device.DeviceStub
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ComboPoolingStrategyTest {
    @Test
    fun `one strategy should return DevicePoolId name equals to DevicePoolId name from base strategy`() {
        val baseStrategy = AbiPoolingStrategy()
        val comboPoolingStrategy = ComboPoolingStrategy(listOf(baseStrategy))
        val abi = "x64"
        val device = DeviceStub(abi = abi)
        comboPoolingStrategy.associate(device).name shouldBeEqualTo abi
    }

    @Test
    fun `two strategies should return DevicePoolId name equals DevicePoolId0_name_DevicePoolId1_name`() {
        val modelStrategy = ModelPoolingStrategy()
        val abiStrategy = AbiPoolingStrategy()
        val comboPoolingStrategy = ComboPoolingStrategy(listOf(modelStrategy, abiStrategy))
        val abi = "x64"
        val model = "TestDeviceModel"
        val device = DeviceStub(abi = abi, model = model)
        comboPoolingStrategy.associate(device).name shouldBeEqualTo "${model}_$abi"
    }

    @Test
    fun `three strategies should return DevicePoolId name equals DevicePoolId0_name_DevicePoolId1_name_DevicePoolId2_name`() {
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
