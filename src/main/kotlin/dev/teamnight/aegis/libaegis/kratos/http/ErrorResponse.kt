package dev.teamnight.aegis.libaegis.kratos.http

import com.fasterxml.jackson.databind.JsonNode

data class ErrorResponse(val error: KratosError)

data class KratosError(
    val code: Long? = null,
    val id: String? = null,
    val message: String,
    val reason: String? = null,
    val request: String? = null,
    val status: String? = null,
    val details: JsonNode? = null
)