package com.malinskiy.marathon.analytics.internal.sub

abstract class TrackerInternalAdapter : TrackerInternal {
    final override fun track(event: Event) {
        when (event) {
            is DeviceConnectedEvent -> trackDeviceConnected(event)
            is DeviceDisconnectedEvent -> trackDeviceDisconnected(event)
            is DevicePreparingEvent -> trackDevicePreparing(event)
            is DeviceProviderPreparingEvent -> trackDeviceProviderPreparing(event)
            is TestEvent -> trackTest(event)
        }
    }

    override fun close() = Unit

    protected open fun trackTest(event: TestEvent) = Unit

    protected open fun trackDeviceProviderPreparing(event: DeviceProviderPreparingEvent) = Unit

    protected open fun trackDevicePreparing(event: DevicePreparingEvent) = Unit

    protected open fun trackDeviceConnected(event: DeviceConnectedEvent) = Unit

    protected open fun trackDeviceDisconnected(event: Event) = Unit
}
