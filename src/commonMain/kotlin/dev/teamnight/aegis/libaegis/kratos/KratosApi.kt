package dev.teamnight.aegis.libaegis.kratos

import dev.teamnight.aegis.libaegis.kratos.http.KratosSession
import io.ktor.client.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

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

    val httpClient: HttpClient = HttpClient()

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

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