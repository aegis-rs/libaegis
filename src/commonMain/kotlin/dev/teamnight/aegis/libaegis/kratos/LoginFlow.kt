package dev.teamnight.aegis.libaegis.kratos

import dev.teamnight.aegis.libaegis.kratos.http.*
import dev.teamnight.aegis.libaegis.kratos.ui.Form
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow

class LoginFlow(
    kratosApi: KratosApi,
    elementFactory: ElementFactory,
    val updateSessionState: suspend (KratosSession, String) -> Unit
) : AbstractFlow(kratosApi, elementFactory) {
    override suspend fun create(): FlowState {
        val response = kratosApi.httpClient.get {
            url {
                takeFrom(Url(kratosApi.baseUrl))
                appendPathSegments("self-service", "login", "api")
            }
            accept(ContentType.Application.Json)
        }

        val body = response.body<String>()

        if (response.status.value != 200) {
            val json = kratosApi.json.decodeFromString<ErrorResponse>(body)
            throw KratosErrorException(json.error)
        }

        val json = kratosApi.json.decodeFromString<LoginFlowResponse>(body)

        this.mutableState = MutableStateFlow(
            LoginFlowStateStep(
                flowId = json.id,
                data = LoginData(
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

    suspend fun completePassword(identifier: String, password: String): FlowState {
        val requestBody = PasswordLoginRequest(password, identifier)
        this.performSubmit(kratosApi.json.encodeToString(requestBody))

        return this.mutableState.value
    }

    suspend fun completeTOTP(identifier: String, code: String): FlowState {
        if (kratosApi.session.value == null) {
            throw IllegalArgumentException("Session is not set in Kratos API class")
        }

        val requestBody = TotpLoginRequest(code)
        this.performSubmit(kratosApi.json.encodeToString(requestBody))

        return this.mutableState.value
    }

    suspend fun performSubmit(jsonBody: String) {
        val response = kratosApi.httpClient.post {
            url {
                takeFrom(Url(kratosApi.baseUrl))
                appendPathSegments("self-service", "login")
                parameters.append("flow", mutableState.value.flowId)
            }
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }

        val body = response.body<String>()

        when (response.status.value) {
            200 -> {
                val json = kratosApi.json.decodeFromString<LoginSubmitResponse>(body)

                this.updateSessionState(json.session, json.sessionToken)
                this.mutableState.emit(
                    LoginFlowStateCompleted(
                        mutableState.value.flowId,
                        json
                    )
                )
            }
            400 -> {
                val json = kratosApi.json.decodeFromString<LoginFlowResponse>(body)

                this.mutableState.emit(
                    LoginFlowStateStep(
                        flowId = json.id,
                        data = LoginData(
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
    }
}

data class LoginFlowStateStep(
    override val flowId: String,
    override val data: LoginData,
) : FlowStateCreated<LoginData>, FlowStateUpdated<LoginData>

data class LoginFlowStateCompleted(
    override val flowId: String,
    override val result: LoginSubmitResponse,
) : FlowStateCompleted<LoginSubmitResponse>

class LoginData(
    flowId: String,
    nextAction: String,
    method: String,
    state: State,
    ui: Form
) : FlowData(flowId, nextAction, method, state, ui)