package dev.teamnight.aegis.libaegis.kratos

import dev.teamnight.aegis.libaegis.kratos.http.ErrorResponse
import dev.teamnight.aegis.libaegis.kratos.http.UiResponse
import dev.teamnight.aegis.libaegis.kratos.ui.Element
import dev.teamnight.aegis.libaegis.kratos.ui.defaultElementFactory
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

/**
 * Flow is an abstract class that represents a flow in Ory Kratos.
 *
 * @property kratosApi The KratosApi instance to use for communication.
 * @property flowId The ID of the flow.
 * @property nextAction The next action to perform.
 * @property nextMethod The HTTP method to use for the next action.
 * @property ui The current UI elements created by the last response.
 * @property elementFactory A function that converts the UI http response into UI element objects.
 */
abstract class Flow(
    val kratosApi: KratosApi,
    var flowId: String? = null,
    var nextAction: String? = null,
    var nextMethod: String? = null,
    var ui: List<Element> = emptyList(),
    var elementFactory: ElementFactory = ::defaultElementFactory
) {
    abstract suspend fun createFlow()

    protected abstract fun updateFlowData(body: String)

    /**
     * Executes the GET request to the specified URL
     */
    protected suspend fun getCreateFlow(url: String) {
        val response = kratosApi.httpClient.get {
            url {
                appendPathSegments(kratosApi.baseUrl, url)
            }
            accept(ContentType.Application.Json)
        }

        val body = response.body<String>()

        if (response.status.value != 200) {
            val json = kratosApi.json.decodeFromString<ErrorResponse>(body)
            throw KratosErrorException(json.error)
        }

        updateFlowData(body)
    }
}

typealias ElementFactory = (UiResponse, Json) -> List<Element>