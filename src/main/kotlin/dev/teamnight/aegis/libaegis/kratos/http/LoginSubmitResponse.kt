package dev.teamnight.aegis.libaegis.kratos.http

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class LoginSubmitResponse(
    val continueWith: String? = null,
    val session: KratosSession,
    val sessionToken: String,
)