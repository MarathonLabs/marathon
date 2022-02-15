package marathon.model

data class Device(
    val apiLevel: String,
    val isTablet: Boolean,
    val serial: String,
    val modelName: String
)
