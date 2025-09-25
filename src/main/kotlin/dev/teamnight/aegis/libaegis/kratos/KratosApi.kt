package dev.teamnight.aegis.libaegis.kratos

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import java.net.http.HttpClient

class KratosApi(val baseUrl: String) {
    val httpClient = HttpClient.newHttpClient()
    val objectMapper = JsonMapper.builder()
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build()!!

    suspend fun createRegistrationFlow(): RegistrationFlow {
        val flow = RegistrationFlow(this)
        flow.createFlow()

        return flow
    }
}