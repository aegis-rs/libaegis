package dev.teamnight.aegis.libaegis.kratos.http

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.OffsetDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class KratosIdentity(
    val createdAt: OffsetDateTime? = null,
    val credentials: JsonNode? = null,
    val id: String,
    val metadataAdmin: String? = null,
    val metadataPublic: String? = null,
    val organizationId: String? = null,
    val recoveryAddresses: List<JsonNode>? = null,
    val schemaId: String,
    val schemaUrl: String,
    val state: String? = null,
    val stateChangedAt: OffsetDateTime? = null,
    val traits: JsonNode,
    val updatedAt: OffsetDateTime? = null,
    val verifiableAddresses: List<JsonNode>? = null
)