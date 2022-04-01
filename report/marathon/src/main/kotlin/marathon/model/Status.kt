package marathon.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Status {
    @SerialName("passed")
    Passed,
    @SerialName("failed")
    Failed,
    @SerialName("ignored")
    Ignored
}
