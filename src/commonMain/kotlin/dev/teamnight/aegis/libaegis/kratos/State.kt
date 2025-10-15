package dev.teamnight.aegis.libaegis.kratos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class State {
    @SerialName("choose_method")
    CHOOSE_METHOD,

    @SerialName("sent_email")
    SENT_EMAIL,

    @SerialName("passed_challenge")
    PASSED_CHALLENGE
}