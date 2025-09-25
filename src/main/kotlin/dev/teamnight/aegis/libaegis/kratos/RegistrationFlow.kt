package dev.teamnight.aegis.libaegis.kratos

import dev.teamnight.aegis.libaegis.kratos.http.*
import kotlinx.coroutines.future.await
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class RegistrationFlow(kratosApi: KratosApi) : Flow(kratosApi) {
    var state: State? = null

    override suspend fun createFlow() {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${kratosApi.baseUrl}/self-service/registration/api"))
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

    private fun updateFlowData(body: String?) {
        val json = kratosApi.objectMapper.readValue(body, RegistrationFlowResponse::class.java)

        this.flowId = json.id
        this.nextAction = json.ui.action
        this.nextMethod = json.ui.method
        this.state = json.state
        this.ui = this.createUiFunction(json.ui)
    }

    suspend fun complete(password: String, username: String, email: String): RegistrationFlowSubmitResponse {
        val requestBody = RegistrationFlowSubmitRequest("password", password, Traits(username, email, username))

        val jsonBody = kratosApi.objectMapper.writeValueAsString(requestBody)

        val request = HttpRequest.newBuilder()
            .uri(URI.create("${kratosApi.baseUrl}/self-service/registration?flow=$flowId"))
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .header("Content-Type", "application/json")
            .build()

        val response = kratosApi.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .await()

        val body = response.body()

        when (response.statusCode()) {
            200 -> Unit
            400 -> {
                updateFlowData(body)
                throw IllegalArgumentException("Registration flow returned 400")
            }

            else -> {
                val json = kratosApi.objectMapper.readValue(body, ErrorResponse::class.java)
                throw KratosErrorException(json.error)
            }
        }

        val json = kratosApi.objectMapper.readValue(body, RegistrationFlowSubmitResponse::class.java)

        return json
    }
}