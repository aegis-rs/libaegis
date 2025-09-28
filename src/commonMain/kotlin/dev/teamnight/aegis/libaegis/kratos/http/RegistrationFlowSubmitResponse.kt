package dev.teamnight.aegis.libaegis.kratos.http

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RegistrationFlowSubmitResponse(
    val continueWith: JsonElement,
    val identity: KratosIdentity,
    val session: KratosSession,
    val sessionToken: String
)