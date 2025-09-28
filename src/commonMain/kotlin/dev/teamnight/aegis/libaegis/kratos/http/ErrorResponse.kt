package dev.teamnight.aegis.libaegis.kratos.http

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ErrorResponse(val error: KratosError)

@Serializable
data class KratosError(
    val code: Long? = null,
    val id: String? = null,
    val message: String,
    val reason: String? = null,
    val request: String? = null,
    val status: String? = null,
    val details: JsonElement? = null
)