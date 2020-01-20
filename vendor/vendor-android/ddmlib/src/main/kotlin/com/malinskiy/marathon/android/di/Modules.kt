import com.malinskiy.marathon.android.AndroidAppInstaller
import com.malinskiy.marathon.android.ddmlib.DdmlibDeviceProvider
import com.malinskiy.marathon.device.DeviceProvider
import org.koin.dsl.module

val ddmlibModule = module {
    single<DeviceProvider?> { DdmlibDeviceProvider(get(), get(), get(), get()) }
    single<AndroidAppInstaller?> { AndroidAppInstaller(get(), get(), get()) }
}
