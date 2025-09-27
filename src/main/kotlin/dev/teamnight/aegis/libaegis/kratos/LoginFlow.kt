package dev.teamnight.aegis.libaegis.kratos

import dev.teamnight.aegis.libaegis.kratos.http.*
import kotlinx.coroutines.future.await
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class LoginFlow(kratosApi: KratosApi) : Flow(kratosApi) {
    var state: State? = null

    override suspend fun createFlow() {
        getCreateFlow("self-service/login/api")
    }

    override fun updateFlowData(body: String?) {
        val json = kratosApi.objectMapper.readValue(body, LoginFlowResponse::class.java)

        this.flowId = json.id
        this.nextAction = json.ui.action
        this.nextMethod = json.ui.method
        this.state = json.state
        this.ui = this.elementFactory(json.ui, kratosApi.objectMapper)
    }

    suspend fun completePassword(identifier: String, password: String): LoginSubmitResponse {
        val requestBody = PasswordLoginRequest(password, identifier)

        val jsonBody = kratosApi.objectMapper.writeValueAsString(requestBody)

        val request = HttpRequest.newBuilder()
            .uri(URI.create("${kratosApi.baseUrl}/self-service/login?flow=$flowId"))
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .header("Content-Type", "application/json")
            .build()

        val response = kratosApi.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .await()

        val body = response.body()

        when (response.statusCode()) {
            200 -> {
                val json = kratosApi.objectMapper.readValue(body, LoginSubmitResponse::class.java)

                kratosApi.sessionToken = json.sessionToken
                kratosApi.session = json.session

                return json
            }

            400 -> {
                updateFlowData(body)
                throw IllegalArgumentException("Login flow returned 400")
            }

            else -> {
                val json = kratosApi.objectMapper.readValue(body, ErrorResponse::class.java)
                throw KratosErrorException(json.error)
            }
        }
    }

    suspend fun completeTOTP(code: String): LoginSubmitResponse {
        if (kratosApi.sessionToken == null) {
            throw IllegalArgumentException("Session token is not set in Kratos API class")
        }

        val requestBody = TotpLoginRequest(code)
        val jsonBody = kratosApi.objectMapper.writeValueAsString(requestBody)

        val request = HttpRequest.newBuilder()
            .uri(URI.create("${kratosApi.baseUrl}/self-service/login?flow=$flowId"))
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .header("Content-Type", "application/json")
            .header("X-Session-Token", kratosApi.sessionToken)
            .build()

        val response = kratosApi.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .await()

        val body = response.body()

        when (response.statusCode()) {
            200 -> {
                val json = kratosApi.objectMapper.readValue(body, LoginSubmitResponse::class.java)

                kratosApi.sessionToken = json.sessionToken
                kratosApi.session = json.session

                return json
            }

            400 -> {
                updateFlowData(body)
                throw IllegalArgumentException("Login flow returned 400")
            }

            else -> {
                val json = kratosApi.objectMapper.readValue(body, ErrorResponse::class.java)
                throw KratosErrorException(json.error)
            }
        }
    }
}