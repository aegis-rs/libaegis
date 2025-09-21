package dev.teamnight.aegis.libaegis.kratos

import dev.teamnight.aegis.libaegis.kratos.http.RegistrationFlowResponse
import dev.teamnight.aegis.libaegis.kratos.http.State
import kotlinx.coroutines.future.await
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class RegistrationFlow(kratosApi: KratosApi) : Flow(kratosApi) {
    var currentState: State? = null

    override suspend fun createFlow() {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${kratosApi.baseUrl}/self-service/registration/api"))
            .GET()
            .build()

        val response = kratosApi.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .await()

        val body = response.body()

        val json = kratosApi.objectMapper.readValue(body, RegistrationFlowResponse::class.java)

        this.flowId = json.id
        this.nextAction = json.ui.action
        this.nextMethod = json.ui.method
        this.currentState = json.state
        this.currentUi = this.createUiFunction(json.ui)
    }
}