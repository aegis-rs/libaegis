package dev.teamnight.aegis.libaegis.kratos

import com.fasterxml.jackson.databind.ObjectMapper
import dev.teamnight.aegis.libaegis.kratos.http.ErrorResponse
import dev.teamnight.aegis.libaegis.kratos.http.UiResponse
import dev.teamnight.aegis.libaegis.kratos.ui.Element
import dev.teamnight.aegis.libaegis.kratos.ui.defaultElementFactory
import kotlinx.coroutines.future.await
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

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

    protected abstract fun updateFlowData(body: String?)

    /**
     * Executes the GET request to the specified URL
     */
    protected suspend fun getCreateFlow(url: String) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${kratosApi.baseUrl}/$url"))
            .GET()
            .build()

        val response = kratosApi.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .await()

        val body = response.body()

        if (response.statusCode() != 200) {
            val json = kratosApi.objectMapper.readValue(body, ErrorResponse::class.java)
            throw KratosErrorException(json.error)
        }

        updateFlowData(body)
    }
}

typealias ElementFactory = (UiResponse, ObjectMapper) -> List<Element>