package dev.teamnight.aegis.libaegis.kratos

import dev.teamnight.aegis.libaegis.kratos.http.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class LoginFlow(kratosApi: KratosApi) : Flow(kratosApi) {
    var state: State? = null

    override suspend fun createFlow() {
        getCreateFlow("self-service/login/api")
    }

    override fun updateFlowData(body: String) {
        val json = kratosApi.json.decodeFromString<LoginFlowResponse>(body)

        this.flowId = json.id
        this.nextAction = json.ui.action
        this.nextMethod = json.ui.method
        this.state = json.state
        this.ui = this.elementFactory(json.ui, kratosApi.json)
    }

    suspend fun completePassword(identifier: String, password: String): LoginSubmitResponse {
        val requestBody = PasswordLoginRequest(password, identifier)

        val jsonBody = kratosApi.json.encodeToString(requestBody)

        val response = kratosApi.httpClient.post {
            url {
                appendPathSegments(kratosApi.baseUrl, "/self-service/login?flow=$flowId")
            }
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }

        val body = response.body<String>()

        when (response.status.value) {
            200 -> {
                val json = kratosApi.json.decodeFromString<LoginSubmitResponse>(body)

                kratosApi.sessionToken = json.sessionToken
                kratosApi.session = json.session

                return json
            }

            400 -> {
                updateFlowData(body)
                throw IllegalArgumentException("Login flow returned 400")
            }

            else -> {
                val json = kratosApi.json.decodeFromString<ErrorResponse>(body)
                throw KratosErrorException(json.error)
            }
        }
    }

    suspend fun completeTOTP(code: String): LoginSubmitResponse {
        if (kratosApi.sessionToken == null) {
            throw IllegalArgumentException("Session token is not set in Kratos API class")
        }

        val requestBody = TotpLoginRequest(code)
        val jsonBody = kratosApi.json.encodeToString(requestBody)

        val response = kratosApi.httpClient.post {
            url {
                appendPathSegments(kratosApi.baseUrl, "/self-service/login?flow=$flowId")
            }
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }

        val body = response.body<String>()

        when (response.status.value) {
            200 -> {
                val json = kratosApi.json.decodeFromString<LoginSubmitResponse>(body)

                kratosApi.sessionToken = json.sessionToken
                kratosApi.session = json.session

                return json
            }

            400 -> {
                updateFlowData(body)
                throw IllegalArgumentException("Login flow returned 400")
            }

            else -> {
                val json = kratosApi.json.decodeFromString<ErrorResponse>(body)
                throw KratosErrorException(json.error)
            }
        }
    }
}