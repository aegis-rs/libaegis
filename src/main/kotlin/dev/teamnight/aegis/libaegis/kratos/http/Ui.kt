package dev.teamnight.aegis.libaegis.kratos.http

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode

class UiResponse(
    val action: String,
    val method: String,
    val messages: Array<UiResponseText>? = null,
    val nodes: Array<UiResponseNode>
) {
    constructor() : this("", "", null, emptyArray())
}

class UiResponseNode(
    val type: String,
    val group: String,
    val attributes: JsonNode,
    val messages: Array<UiResponseText>,
    val meta: JsonNode
) {
    constructor() : this("", "", NullNode.getInstance(), emptyArray(), NullNode.getInstance())
}

class UiResponseText(
    val id: Long,
    val text: String,
    val type: UiResponseTextType
) {
    constructor() : this(0, "", UiResponseTextType.INFO)
}

enum class UiResponseTextType {
    INFO,
    ERROR,
    SUCCESS
}