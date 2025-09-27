package dev.teamnight.aegis.libaegis.kratos

import dev.teamnight.aegis.libaegis.kratos.http.*
import kotlinx.coroutines.future.await
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

class RegistrationFlow(kratosApi: KratosApi) : Flow(kratosApi) {
    var state: State? = null

    private var username: String? = null
    private var email: String? = null

    override suspend fun createFlow() {
        getCreateFlow("self-service/registration/api")
    }

    override fun updateFlowData(body: String?) {
        val json = kratosApi.objectMapper.readValue(body, RegistrationFlowResponse::class.java)

        this.flowId = json.id
        this.nextAction = json.ui.action
        this.nextMethod = json.ui.method
        this.state = json.state
        this.ui = this.elementFactory(json.ui, kratosApi.objectMapper)
    }

    suspend fun update(username: String, email: String) {
        val requestBody = ProfileRegistrationFlowSubmitRequest(Traits(username, email, username))

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
            200 -> {
                //This is cause Ory Kratos returns an 400 error with the new fields, cause 200 would mean that
                //the registration was finished
                throw IllegalStateException("Registration flow returned 200 even though it should have returned 400.")
            }

            400 -> {
                this.username = username
                this.email = email
                updateFlowData(body)
            }

            else -> {
                val json = kratosApi.objectMapper.readValue(body, ErrorResponse::class.java)
                throw KratosErrorException(json.error)
            }
        }
    }

    suspend fun complete(password: String): RegistrationFlowSubmitResponse {
        Objects.requireNonNull(username)
        Objects.requireNonNull(email)

        val requestBody =
            PasswordRegistrationFlowSubmitRequest(Traits(this.username!!, this.email!!, this.username!!), password)

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