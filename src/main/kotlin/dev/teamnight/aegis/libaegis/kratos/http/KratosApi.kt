package dev.teamnight.aegis.libaegis.kratos.http

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.future.await
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class KratosApi(val baseUrl: String) {
    val httpClient = HttpClient.newHttpClient()
    val objectMapper = ObjectMapper()

    suspend fun createRegistrationFlow(): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/self-service/registration/api"))
            .GET()
            .build()

        val response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .await()

        val body = response.body()

        val node = objectMapper.readTree(body)

        return node.get("id").asText()
    }

    suspend fun submitRegistrationFlow(): String {
        return "null"
    }
}