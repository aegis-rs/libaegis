package dev.teamnight.aegis.libaegis.kratos.http

open class RegistrationFlowSubmitRequest(
    val method: String,
    val traits: Traits
)

class ProfileRegistrationFlowSubmitRequest(
    traits: Traits,
) : RegistrationFlowSubmitRequest("profile", traits)

class PasswordRegistrationFlowSubmitRequest(
    traits: Traits,
    val password: String,
) : RegistrationFlowSubmitRequest("password", traits)