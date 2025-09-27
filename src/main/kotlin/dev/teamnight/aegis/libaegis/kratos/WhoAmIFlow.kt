package dev.teamnight.aegis.libaegis.kratos

import dev.teamnight.aegis.libaegis.kratos.http.KratosSession

class WhoAmIFlow(kratosApi: KratosApi) : Flow(kratosApi) {
    override suspend fun createFlow() {
        try {
            getCreateFlow("sessions/whoami")
        } catch (e: KratosErrorException) {
            kratosApi.session = null
            throw KratosErrorException(e.error)
        }
    }

    override fun updateFlowData(body: String?) {
        val json = kratosApi.objectMapper.readValue(body, KratosSession::class.java)

        kratosApi.session = json
    }
}