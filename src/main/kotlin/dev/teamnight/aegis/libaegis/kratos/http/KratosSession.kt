package dev.teamnight.aegis.libaegis.kratos.http

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.OffsetDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class KratosSession(
    val active: Boolean? = null,
    val authenticatedAt: OffsetDateTime? = null,
    val authenticatorAssuranceLevel: String? = null,
    val expiresAt: OffsetDateTime? = null,
    val id: String,
    val identity: KratosIdentity? = null,
    val issuedAt: OffsetDateTime? = null,
    val tokenized: String? = null,
)