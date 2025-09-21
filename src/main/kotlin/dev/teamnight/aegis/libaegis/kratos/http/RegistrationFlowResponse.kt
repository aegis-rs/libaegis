package dev.teamnight.aegis.libaegis.kratos.http

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

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
)

enum class State {
    CHOOSE_METHOD,
    SENT_EMAIL,
    PASSED_CHALLENGE
}