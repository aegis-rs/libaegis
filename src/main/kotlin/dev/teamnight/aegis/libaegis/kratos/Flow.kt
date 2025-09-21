package dev.teamnight.aegis.libaegis.kratos

import dev.teamnight.aegis.libaegis.kratos.http.UiResponse
import dev.teamnight.aegis.libaegis.kratos.ui.Element
import dev.teamnight.aegis.libaegis.kratos.ui.createUi

/**
 * Flow is an abstract class that represents a flow in Ory Kratos.
 *
 * @property kratosApi The KratosApi instance to use for communication.
 * @property flowId The ID of the flow.
 * @property nextAction The next action to perform.
 * @property nextMethod The HTTP method to use for the next action.
 * @property currentUi The current UI elements created by the last response.
 * @property createUiFunction A function that converts the UI http response into UI element objects.
 */
abstract class Flow(
    val kratosApi: KratosApi,
    var flowId: String? = null,
    var nextAction: String? = null,
    var nextMethod: String? = null,
    var currentUi: List<Element> = emptyList(),
    var createUiFunction: (UiResponse) -> List<Element> = ::createUi
) {
    abstract suspend fun createFlow()
}