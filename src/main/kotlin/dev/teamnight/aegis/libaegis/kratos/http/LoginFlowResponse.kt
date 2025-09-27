package dev.teamnight.aegis.libaegis.kratos.http

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dev.teamnight.aegis.libaegis.kratos.AAL
import dev.teamnight.aegis.libaegis.kratos.LoginMethod
import dev.teamnight.aegis.libaegis.kratos.State

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
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
) {
    constructor() : this(null, null, "", "", "", null, null, "", null, null, State.CHOOSE_METHOD, "", UiResponse())
}