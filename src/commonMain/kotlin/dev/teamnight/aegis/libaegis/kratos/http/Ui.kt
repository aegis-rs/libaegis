package dev.teamnight.aegis.libaegis.kratos.http

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject


@Serializable
class UiResponse(
    val action: String,
    val method: String,
    val messages: Array<UiResponseText>? = null,
    val nodes: Array<UiResponseNode>
)

@Serializable
class UiResponseNode(
    val type: String,
    val group: String,
    val attributes: JsonObject,
    val messages: Array<UiResponseText>,
    val meta: JsonObject
)

@Serializable
class UiResponseText(
    val id: Long,
    val text: String,
    val type: UiResponseTextType
)

enum class UiResponseTextType {
    @SerialName("info")
    INFO,

    @SerialName("error")
    ERROR,

    @SerialName("success")
    SUCCESS
}