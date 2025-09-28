package dev.teamnight.aegis.libaegis.kratos

import dev.teamnight.aegis.libaegis.kratos.http.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class RegistrationFlow(kratosApi: KratosApi) : Flow(kratosApi) {
    var state: State? = null

    private var username: String? = null
    private var email: String? = null

    override suspend fun createFlow() {
        getCreateFlow("self-service/registration/api")
    }

    override fun updateFlowData(body: String) {
        val json = kratosApi.json.decodeFromString<RegistrationFlowResponse>(body)

        this.flowId = json.id
        this.nextAction = json.ui.action
        this.nextMethod = json.ui.method
        this.state = json.state
        this.ui = this.elementFactory(json.ui, kratosApi.json)
    }

    suspend fun update(username: String, email: String) {
        val requestBody = ProfileRegistrationFlowSubmitRequest(Traits(username, email, username))

        val jsonBody = kratosApi.json.encodeToString(requestBody)

        val response = kratosApi.httpClient.post {
            url {
                appendPathSegments(kratosApi.baseUrl, "/self-service/registration?flow=$flowId")
            }
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }

        val body = response.body<String>()

        when (response.status.value) {
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
                val json = kratosApi.json.decodeFromString<ErrorResponse>(body)
                throw KratosErrorException(json.error)
            }
        }
    }

    suspend fun complete(password: String): RegistrationFlowSubmitResponse {
        requireNotNull(username)
        requireNotNull(email)

        val requestBody =
            PasswordRegistrationFlowSubmitRequest(Traits(this.username!!, this.email!!, this.username!!), password)

        val jsonBody = kratosApi.json.encodeToString(requestBody)

        val response = kratosApi.httpClient.post {
            url {
                appendPathSegments(kratosApi.baseUrl, "/self-service/registration?flow=$flowId")
            }
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }

        val body = response.body<String>()

        when (response.status.value) {
            200 -> Unit
            400 -> {
                updateFlowData(body)
                throw IllegalArgumentException("Registration flow returned 400")
            }

            else -> {
                val json = kratosApi.json.decodeFromString<ErrorResponse>(body)
                throw KratosErrorException(json.error)
            }
        }

        val json = kratosApi.json.decodeFromString<RegistrationFlowSubmitResponse>(body)

        return json
    }
}