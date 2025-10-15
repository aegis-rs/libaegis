package dev.teamnight.aegis.libaegis.kratos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AAL {
    @SerialName("aal1")
    AAL1,

    @SerialName("aal2")
    AAL2,

    @SerialName("aal3")
    AAL3,

    @SerialName("highest_available")
    HIGHEST_AVAILABLE
}