package dev.teamnight.aegis.libaegis.kratos.http

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dev.teamnight.aegis.libaegis.kratos.State

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class RegistrationFlowResponse(
    val id: String,
    val type: String,
    val expiresAt: String,
    val issuedAt: String,
    val requestUrl: String,
    val ui: UiResponse,
    val organizationId: String?,
    val state: State
) {
    constructor() : this("", "", "", "", "", UiResponse(), null, State.CHOOSE_METHOD)
}

