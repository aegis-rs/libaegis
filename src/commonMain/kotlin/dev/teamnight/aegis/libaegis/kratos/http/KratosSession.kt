package dev.teamnight.aegis.libaegis.kratos.http

import kotlinx.serialization.Serializable

@Serializable
data class KratosSession(
    val active: Boolean? = null,
    val authenticatedAt: String? = null,
    val authenticatorAssuranceLevel: String? = null,
    val expiresAt: String? = null,
    val id: String,
    val identity: KratosIdentity? = null,
    val issuedAt: String? = null,
    val tokenized: String? = null,
)