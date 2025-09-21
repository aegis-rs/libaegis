package dev.teamnight.aegis.libaegis.kratos

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.http.HttpClient

class KratosApi(val baseUrl: String) {
    val httpClient = HttpClient.newHttpClient()
    val objectMapper = ObjectMapper()

    suspend fun createRegistrationFlow(): RegistrationFlow {
        val flow = RegistrationFlow(this)
        flow.createFlow()

        return flow
    }

    suspend fun submitRegistrationFlow(): String {
        return "null"
    }
}