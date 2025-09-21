package dev.teamnight.aegis.libaegis.kratos.ui

import com.fasterxml.jackson.databind.JsonNode
import dev.teamnight.aegis.libaegis.kratos.http.UiResponse

open class Element(
    val group: String,
    val attributes: Map<String, JsonNode>,
    val messages: Array<Text>,
    val meta: ElementMeta
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
    meta: ElementMeta,
) : Element(group, attributes, messages, meta)

class EmailFieldElement(
    group: String,
    attributes: Map<String, JsonNode>,
    messages: Array<Text>,
    meta: ElementMeta,
) : Element(group, attributes, messages, meta)

class HiddenFieldElement(
    group: String,
    attributes: Map<String, JsonNode>,
    messages: Array<Text>,
    meta: ElementMeta,
) : Element(group, attributes, messages, meta)

class SubmitButtonElement(
    group: String,
    attributes: Map<String, JsonNode>,
    messages: Array<Text>,
    meta: ElementMeta,
) : Element(group, attributes, messages, meta)

fun createUi(response: UiResponse): List<Element> {
    return emptyList()
}