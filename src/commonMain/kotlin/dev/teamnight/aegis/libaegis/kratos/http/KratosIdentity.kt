package dev.teamnight.aegis.libaegis.kratos.http

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class KratosIdentity(
    val createdAt: String? = null,
    val credentials: JsonElement? = null,
    val id: String,
    val metadataAdmin: String? = null,
    val metadataPublic: String? = null,
    val organizationId: String? = null,
    val recoveryAddresses: List<JsonElement>? = null,
    val schemaId: String,
    val schemaUrl: String,
    val state: String? = null,
    val stateChangedAt: String? = null,
    val traits: JsonElement,
    val updatedAt: String? = null,
    val verifiableAddresses: List<JsonElement>? = null
)