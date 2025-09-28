package dev.teamnight.aegis.libaegis.kratos.http

import kotlinx.serialization.Serializable

@Serializable
data class LoginSubmitResponse(
    val continueWith: String? = null,
    val session: KratosSession,
    val sessionToken: String,
)