package dev.teamnight.aegis.libaegis.kratos.http

import kotlinx.serialization.Serializable

/**
 * Traits specific to Aegis.
 */
@Serializable
data class Traits(
    val username: String,
    val email: String,
    val nickname: String
)
