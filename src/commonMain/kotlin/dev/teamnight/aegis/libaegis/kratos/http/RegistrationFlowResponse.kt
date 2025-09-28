package dev.teamnight.aegis.libaegis.kratos.http

import dev.teamnight.aegis.libaegis.kratos.State
import kotlinx.serialization.Serializable

@Serializable
class RegistrationFlowResponse(
    val id: String,
    val type: String,
    val expiresAt: String,
    val issuedAt: String,
    val requestUrl: String,
    val ui: UiResponse,
    val organizationId: String?,
    val state: State
)

