package marathon.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val apiLevel: String,
    @SerialName("isTable") val isTablet: Boolean,
    val serial: String,
    val modelName: String
)
