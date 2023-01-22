import com.malinskiy.marathon.android.ddmlib.DdmlibDeviceProvider
import com.malinskiy.marathon.device.DeviceProvider
import org.koin.dsl.module

val ddmlibModule = module {
    factory <DeviceProvider> { DdmlibDeviceProvider(get(), get(), get(), get(), get()) }
}
