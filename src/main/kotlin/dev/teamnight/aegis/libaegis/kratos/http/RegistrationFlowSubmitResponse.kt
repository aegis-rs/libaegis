package dev.teamnight.aegis.libaegis.kratos.http

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RegistrationFlowSubmitResponse(
    val continueWith: JsonNode,
    val identity: KratosIdentity,
    val session: KratosSession,
    val sessionToken: String
)