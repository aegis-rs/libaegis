package dev.teamnight.aegis.libaegis.kratos

import dev.teamnight.aegis.libaegis.kratos.http.ErrorResponse
import dev.teamnight.aegis.libaegis.kratos.http.KratosSession
import dev.teamnight.aegis.libaegis.kratos.ui.defaultElementFactory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

class KratosApi(
    val baseUrl: String,
    private val _session: MutableStateFlow<KratosSession?> = MutableStateFlow(null),
    private val _sessionToken: MutableStateFlow<String?> = MutableStateFlow(null)
) {
    val session
        get() = _session.asStateFlow()

    val token
        get() = _sessionToken.asStateFlow()

    val httpClient: HttpClient = HttpClient()

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        namingStrategy = JsonNamingStrategy.SnakeCase
        classDiscriminatorMode = ClassDiscriminatorMode.NONE
    }

    suspend fun createRegistrationFlow(): RegistrationFlow {
        val flow = RegistrationFlow(this, ::defaultElementFactory) { session, token ->
            _session.emit(session)
            _sessionToken.emit(token)
        }
        flow.create()

        return flow
    }

    suspend fun createLoginFlow(): LoginFlow {
        val flow = LoginFlow(this, ::defaultElementFactory) { session, token ->
            _session.emit(session)
            _sessionToken.emit(token)
        }
        flow.create()

        return flow
    }

    suspend fun refreshSessionInfo() {
        requireNotNull(session.value) { "Session is null, cannot refresh." }

        val response = this.httpClient.get {
            url {
                takeFrom(Url(baseUrl))
                appendPathSegments("sessions", "whoami")
            }
            accept(ContentType.Application.Json)
            headers {
                append("X-Session-Token", token.value!!)
            }
        }

        val body = response.body<String>()

        if (response.status.value != 200) {
            val json = this.json.decodeFromString<ErrorResponse>(body)
            throw KratosErrorException(json.error)
        }

        val json = this.json.decodeFromString<KratosSession>(body)

        this._session.emit(json)
    }

    suspend fun isReady(): Boolean {
        val response = this.httpClient.get {
            url {
                takeFrom(Url(baseUrl))
                appendPathSegments("health", "ready")
            }
            accept(ContentType.Application.Json)
        }

        return response.status.value == 200
    }
}