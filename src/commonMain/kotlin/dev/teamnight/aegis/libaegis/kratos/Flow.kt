package dev.teamnight.aegis.libaegis.kratos

import dev.teamnight.aegis.libaegis.kratos.http.UiResponse
import dev.teamnight.aegis.libaegis.kratos.ui.Form
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * Flow represents an Ory Kratos flow.
 */
interface Flow {
    /**
     * Creates a new flow by calling the Ory Kratos API.
     */
    suspend fun create(): FlowState

    /**
     * Returns the current state as state flow.
     */
    fun getStateAsStateFlow(): StateFlow<FlowState>

    /**
     * Returns the state of the last response of this flow.
     */
    val state: FlowState
}

/**
 * AbstractFlow provides some basic functionality for the Flow interface.
 *
 * @property kratosApi The [KratosApi] instance to use for communication.
 * @property mutableState A [MutableStateFlow] that represents the current flow state.
 *
 * Example code for Flow#create:
 * ```kotlin
 *     protected suspend fun create() {
 *         val response = kratosApi.httpClient.get {
 *             url {
 *                 appendPathSegments(kratosApi.baseUrl, "self-service", "registration", "api")
 *             }
 *             accept(ContentType.Application.Json)
 *         }
 *
 *         val body = response.body<String>()
 *
 *         if (response.status.value != 200) {
 *             val json = kratosApi.json.decodeFromString<ErrorResponse>(body)
 *             throw KratosErrorException(json.error)
 *         }
 *
 *         updateFlowData(body)
 *     }
 * ```
 */
abstract class AbstractFlow(
    protected val kratosApi: KratosApi,
    protected val elementFactory: ElementFactory
) : Flow {
    protected lateinit var mutableState: MutableStateFlow<FlowState>

    override val state: FlowState
        get() = mutableState.value

    override fun getStateAsStateFlow(): StateFlow<FlowState> {
        return mutableState.asStateFlow()
    }
}

/**
 * Abstract class for flow data that might be returned after the create or update of a flow.
 *
 * @property flowId The ID of the flow.
 * @property nextAction The next url to call
 * @property method The method to use for the next action.
 * @property state State enum for the flows. This is different from FlowState, it is an enum value provided by Ory
 *                 Kratos.
 * @property ui The UI elements to display.
 */
abstract class FlowData(
    val flowId: String,
    val nextAction: String,
    val method: String,
    val state: State,
    val ui: Form,
)

typealias ElementFactory = (UiResponse, Json) -> Form