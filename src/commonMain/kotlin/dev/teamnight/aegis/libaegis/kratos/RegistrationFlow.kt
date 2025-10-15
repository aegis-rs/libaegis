package dev.teamnight.aegis.libaegis.kratos

import dev.teamnight.aegis.libaegis.kratos.http.*
import dev.teamnight.aegis.libaegis.kratos.ui.Element
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow

class RegistrationFlow(
    kratosApi: KratosApi,
    elementFactory: ElementFactory,
    val updateSessionState: suspend (KratosSession, String) -> Unit
) : AbstractFlow(kratosApi, elementFactory) {
    override suspend fun create(): FlowState {
        val response = kratosApi.httpClient.get {
            url {
                takeFrom(Url(kratosApi.baseUrl))
                appendPathSegments("self-service", "registration", "api")
            }
            accept(ContentType.Application.Json)
        }

        val body = response.body<String>()

        if (response.status.value != 200) {
            val json = kratosApi.json.decodeFromString<ErrorResponse>(body)
            throw KratosErrorException(json.error)
        }

        val json = kratosApi.json.decodeFromString<RegistrationFlowResponse>(body)

        this.mutableState = MutableStateFlow(
            RegistrationFlowCreated(
                flowId = json.id,
                data = RegistrationData(
                    flowId = json.id,
                    nextAction = json.ui.action,
                    method = json.ui.method,
                    state = json.state,
                    ui = this.elementFactory(json.ui, kratosApi.json)
                )
            )
        )

        return this.mutableState.value
    }

    /**
     * Updates the registration flow depending on which [RegistrationParams] was passed.
     *
     * If the registration request is a [RegistrationParams.Profile], the method is set to "profile".
     * If the registration request is a [RegistrationParams.Password], the method is set to "password".
     *
     * @throws KratosErrorException if the flow returned an error response.
     */
    suspend fun update(request: RegistrationParams): FlowState {
        val requestBody = when (request) {
            is RegistrationParams.Profile -> ProfileRegistrationFlowSubmitRequest(request.traits)
            is RegistrationParams.Password -> PasswordRegistrationFlowSubmitRequest(request.traits, request.password)
        }

        val jsonBody = kratosApi.json.encodeToString(requestBody)

        val response = kratosApi.httpClient.post {
            url {
                takeFrom(Url(kratosApi.baseUrl))
                appendPathSegments("self-service", "registration")
                parameters.append("flow", mutableState.value.flowId)
            }
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }

        val body = response.body<String>()

        when (response.status.value) {
            200 -> {
                val json = kratosApi.json.decodeFromString<RegistrationFlowSubmitResponse>(body)

                this.updateSessionState(json.session, json.sessionToken)
                this.mutableState.emit(
                    RegistrationFlowCompleted(
                        mutableState.value.flowId,
                        json
                    )
                )
            }
            400 -> {
                val json = kratosApi.json.decodeFromString<RegistrationFlowResponse>(body)

                this.mutableState.emit(
                    RegistrationFlowUpdated(
                        flowId = json.id,
                        data = IdentityRegistrationData(
                            username = requestBody.traits.username,
                            email = requestBody.traits.email,
                            flowId = json.id,
                            nextAction = json.ui.action,
                            method = json.ui.method,
                            state = json.state,
                            ui = this.elementFactory(json.ui, kratosApi.json)
                        )
                    )
                )
            }
            else -> {
                val json = kratosApi.json.decodeFromString<ErrorResponse>(body)
                throw KratosErrorException(json.error)
            }
        }

        return mutableState.value
    }
}

sealed class RegistrationParams {
    data class Profile(val traits: Traits) : RegistrationParams()
    data class Password(val traits: Traits, val password: String) : RegistrationParams()
}

data class RegistrationFlowCreated(
    override val flowId: String,
    override val data: RegistrationData,
) : FlowStateCreated<RegistrationData>

data class RegistrationFlowUpdated(
    override val flowId: String,
    override val data: IdentityRegistrationData
) : FlowStateUpdated<IdentityRegistrationData>

data class RegistrationFlowCompleted(
    override val flowId: String,
    override val result: RegistrationFlowSubmitResponse,
) : FlowStateCompleted<RegistrationFlowSubmitResponse>

open class RegistrationData(
    flowId: String,
    nextAction: String,
    method: String,
    state: State,
    ui: List<Element>,
) : FlowData(flowId, nextAction, method, state, ui)

class IdentityRegistrationData(
    val username: String,
    val email: String, flowId: String, nextAction: String, method: String, state: State, ui: List<Element>,
) : RegistrationData(flowId, nextAction, method, state, ui) {
    fun toPasswordParams(password: String): RegistrationParams =
        RegistrationParams.Password(Traits(username, email, username), password)
}