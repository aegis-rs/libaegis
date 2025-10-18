package dev.teamnight.aegis.libaegis.kratos.ui

import dev.teamnight.aegis.libaegis.kratos.http.UiResponse
import dev.teamnight.aegis.libaegis.kratos.http.UiResponseText
import dev.teamnight.aegis.libaegis.kratos.http.UiResponseTextType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive

open class Element(
    val group: String,
    val attributes: Map<String, String>,
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
    attributes: Map<String, String>,
    messages: Array<Text>,
    meta: ElementMeta? = null,
) : Element(group, attributes, messages, meta)

class PasswordFieldElement(
    group: String,
    attributes: Map<String, String>,
    messages: Array<Text>,
    meta: ElementMeta? = null,
) : Element(group, attributes, messages, meta)

class EmailFieldElement(
    group: String,
    attributes: Map<String, String>,
    messages: Array<Text>,
    meta: ElementMeta? = null,
) : Element(group, attributes, messages, meta)

class HiddenFieldElement(
    group: String,
    attributes: Map<String, String>,
    messages: Array<Text>,
    meta: ElementMeta? = null,
) : Element(group, attributes, messages, meta)

class SubmitButtonElement(
    group: String,
    attributes: Map<String, String>,
    messages: Array<Text>,
    meta: ElementMeta? = null,
) : Element(group, attributes, messages, meta)

//TODO: Refactor
fun defaultElementFactory(response: UiResponse, mapper: Json): Form {
    return Form(
        action = response.action,
        method = response.method,
        messages = response.messages?.map { convertUiResponseTextToText(it) } ?: emptyList(),
        nodes = response.nodes.map { node ->
            val attributes = node.attributes.asSequence().associate { it.key to it.value.jsonPrimitive.content }
            val messages = node.messages.map { msg ->
                convertUiResponseTextToText(msg)
            }.toTypedArray()
            val meta = if (node.meta["label"] != null) {
                val label = mapper.decodeFromJsonElement<UiResponseText>(node.meta["label"]!!)
                ElementMeta(label = convertUiResponseTextToText(label))
            } else null

            when (node.type) {
                "input" -> when (node.attributes["type"]?.jsonPrimitive?.content) {
                    "text" -> TextFieldElement(node.group, attributes, messages, meta)
                    "email" -> EmailFieldElement(node.group, attributes, messages, meta)
                    "password" -> PasswordFieldElement(node.group, attributes, messages, meta)
                    "hidden" -> HiddenFieldElement(node.group, attributes, messages, meta)
                    "submit" -> SubmitButtonElement(node.group, attributes, messages, meta)
                    else -> Element(node.group, attributes, messages, meta)
                }

                else -> TextFieldElement(node.group, emptyMap(), emptyArray(), null)
            }
        },
    )
}

private fun convertUiResponseTextToText(text: UiResponseText): Text {
    return Text(
        text.id.toString(),
        text.text,
        when (text.type) {
            UiResponseTextType.SUCCESS -> TextType.SUCCESS
            UiResponseTextType.ERROR -> TextType.ERROR
            else -> TextType.INFO
        }
    )
}