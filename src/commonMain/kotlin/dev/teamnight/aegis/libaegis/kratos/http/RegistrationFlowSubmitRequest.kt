package dev.teamnight.aegis.libaegis.kratos.http

import kotlinx.serialization.Serializable

@Serializable
sealed class RegistrationFlowSubmitRequest(
    val method: String
) {
    abstract val traits: Traits
}

@Serializable
class ProfileRegistrationFlowSubmitRequest(
    override val traits: Traits,
) : RegistrationFlowSubmitRequest("profile")

@Serializable
class PasswordRegistrationFlowSubmitRequest(
    override val traits: Traits,
    val password: String,
) : RegistrationFlowSubmitRequest("password")