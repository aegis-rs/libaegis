package dev.teamnight.aegis.libaegis.kratos.http

import java.time.OffsetDateTime

data class KratosSession(
    val active: Boolean,
    val authenticatedAt: OffsetDateTime,
    val authenticatorAssuranceLevel: String,
    val expiresAt: OffsetDateTime,
    val id: String,
    val identity: KratosIdentity,
    val issuedAt: OffsetDateTime,
    val tokenized: String
)