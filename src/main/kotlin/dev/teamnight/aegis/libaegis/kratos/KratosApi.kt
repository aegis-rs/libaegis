package dev.teamnight.aegis.libaegis.kratos

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dev.teamnight.aegis.libaegis.kratos.http.KratosSession
import java.net.http.HttpClient

class KratosApi(
    val baseUrl: String,
    var sessionToken: String? = null,
    private var _session: KratosSession? = null,
) {
    var session: KratosSession?
        get() = _session
        set(value) {
            _session = value
            sessionToken = value?.id
        }

    val httpClient: HttpClient = HttpClient.newHttpClient()
    val objectMapper: ObjectMapper = JsonMapper.builder()
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .enable(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build()!!
        .registerKotlinModule()
        .registerModule(JavaTimeModule())

    suspend fun createRegistrationFlow(): RegistrationFlow {
        val flow = RegistrationFlow(this)
        flow.createFlow()

        return flow
    }

    suspend fun createLoginFlow(): LoginFlow {
        val flow = LoginFlow(this)
        flow.createFlow()

        return flow
    }

    suspend fun refreshSession() {
        val flow = WhoAmIFlow(this)
        flow.createFlow()
    }
}