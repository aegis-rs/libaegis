package dev.teamnight.aegis.libaegis.kratos.http

import dev.teamnight.aegis.libaegis.kratos.AAL
import dev.teamnight.aegis.libaegis.kratos.LoginMethod
import dev.teamnight.aegis.libaegis.kratos.State
import kotlinx.serialization.Serializable

@Serializable
data class LoginFlowResponse(
    val active: LoginMethod? = null,
    val createdAt: String? = null,
    val expiresAt: String,
    val id: String,
    val issuedAt: String,
    val organizationId: String? = null,
    val refresh: Boolean? = null,
    val requestUrl: String,
    val requestedAAL: AAL? = null,
    val returnTo: String? = null,
    val state: State,
    val type: String,
    val ui: UiResponse,
    val updatedAt: String? = null
)