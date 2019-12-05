import com.malinskiy.marathon.android.ddmlib.DdmlibDeviceProvider
import com.malinskiy.marathon.device.DeviceProvider
import org.koin.dsl.module

val ddmlibModule = module {
    single<DeviceProvider?> { DdmlibDeviceProvider(get(), get()) }
}