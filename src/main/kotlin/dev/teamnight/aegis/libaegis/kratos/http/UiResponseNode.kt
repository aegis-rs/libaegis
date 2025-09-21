package dev.teamnight.aegis.libaegis.kratos.http

import com.fasterxml.jackson.databind.JsonNode

class UiResponse(
    val action: String,
    val method: String,
    val nodes: Array<UiResponseNode>
)

class UiResponseNode(
    val type: String,
    val group: String,
    val attributes: JsonNode,
    val messages: Array<UiResponseText>,
    val meta: JsonNode
)

class UiResponseText(
    val id: Long,
    val text: String,
    val type: UiResponseTextType
)

enum class UiResponseTextType {
    INFO,
    ERROR,
    SUCCESS
}