package dev.teamnight.aegis.libaegis.kratos.ui

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dev.teamnight.aegis.libaegis.kratos.http.UiResponse
import dev.teamnight.aegis.libaegis.kratos.http.UiResponseTextType

open class Element(
    val group: String,
    val attributes: Map<String, JsonNode>,
    val messages: Array<Text>,
    val meta: ElementMeta? = null
)

class Text(
    val id: String,
    val text: String,
    val type: TextType
)

enum class TextType {
    INFO,
    ERROR,
    SUCCESS
}

class ElementMeta(
    val label: Text
)

class TextFieldElement(
    group: String,
    attributes: Map<String, JsonNode>,
    messages: Array<Text>,
    meta: ElementMeta? = null,
) : Element(group, attributes, messages, meta)

class PasswordFieldElement(
    group: String,
    attributes: Map<String, JsonNode>,
    messages: Array<Text>,
    meta: ElementMeta? = null,
) : Element(group, attributes, messages, meta)

class EmailFieldElement(
    group: String,
    attributes: Map<String, JsonNode>,
    messages: Array<Text>,
    meta: ElementMeta? = null,
) : Element(group, attributes, messages, meta)

class HiddenFieldElement(
    group: String,
    attributes: Map<String, JsonNode>,
    messages: Array<Text>,
    meta: ElementMeta? = null,
) : Element(group, attributes, messages, meta)

class SubmitButtonElement(
    group: String,
    attributes: Map<String, JsonNode>,
    messages: Array<Text>,
    meta: ElementMeta? = null,
) : Element(group, attributes, messages, meta)

fun defaultElementFactory(response: UiResponse, mapper: ObjectMapper): List<Element> {
    return response.nodes.map { node ->
        val attributes = node.attributes.properties().associate { it.key to it.value }
        val messages = node.messages.map { msg ->
            Text(
                msg.id.toString(),
                msg.text,
                when (msg.type) {
                    UiResponseTextType.SUCCESS -> TextType.SUCCESS
                    UiResponseTextType.ERROR -> TextType.ERROR
                    else -> TextType.INFO
                }
            )
        }.toTypedArray()
        val meta = if (node.meta.get("label") != null) {
            ElementMeta(label = mapper.treeToValue(node.meta.get("label"), Text::class.java))
        } else null

        when (node.type) {
            "input" -> when (node.attributes.get("type")?.asText()) {
                "text" -> TextFieldElement(node.group, attributes, messages, meta)
                "email" -> EmailFieldElement(node.group, attributes, messages, meta)
                "password" -> PasswordFieldElement(node.group, attributes, messages, meta)
                "hidden" -> HiddenFieldElement(node.group, attributes, messages, meta)
                "submit" -> SubmitButtonElement(node.group, attributes, messages, meta)
                else -> Element(node.group, attributes, messages, meta)
            }

            else -> TextFieldElement(node.group, emptyMap(), emptyArray(), null)
        }
    }
}