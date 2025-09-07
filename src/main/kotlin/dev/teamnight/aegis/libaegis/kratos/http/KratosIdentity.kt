package dev.teamnight.aegis.libaegis.kratos.http

import com.fasterxml.jackson.databind.JsonNode
import java.time.OffsetDateTime

data class KratosIdentity(
    val createdAt: OffsetDateTime,
    val credentials: JsonNode,
    val id: String,
    val metadataAdmin: String,
    val metadataPublic: String,
    val organizationId: String,
    val recoveryAddresses: List<JsonNode>,
    val schemaId: String,
    val schemaUrl: String,
    val state: String,
    val stateChangedAt: OffsetDateTime,
    val traits: JsonNode,
    val updatedAt: OffsetDateTime,
    val verificationAddresses: List<JsonNode>
)