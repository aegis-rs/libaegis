import dev.teamnight.aegis.libaegis.kratos.KratosApi
import dev.teamnight.aegis.libaegis.kratos.LoginFlowStateCompleted
import dev.teamnight.aegis.libaegis.kratos.RegistrationFlowCompleted
import dev.teamnight.aegis.libaegis.kratos.RegistrationParams
import dev.teamnight.aegis.libaegis.kratos.http.Traits
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class KratosTests {

    @Test
    fun testSuccessfulHealthReady() {
        val kratosApi = KratosApi("http://localhost:4433")

        runBlocking {
            assertTrue {
                kratosApi.isReady()
            }
        }
    }

    @Test
    fun testSuccessfulRegistration() {
        val kratosApi = KratosApi("http://localhost:4433")

        runBlocking {
            val flow = kratosApi.createRegistrationFlow()

            val traits = Traits(
                "teamnight",
                "admin@teamnight.dev",
                "teamnight"
            )

            val step1 = flow.update(RegistrationParams.Profile(traits))

            val response = flow.update(
                RegistrationParams.Password(
                    traits = traits,
                    password = "a5BNzu357S"
                )
            )

            assertTrue { response is RegistrationFlowCompleted }
            assertTrue {
                val state = response as RegistrationFlowCompleted

                state.result.sessionToken.isNotEmpty()
            }
        }
    }

    @Test
    fun testSuccessfulLogin() {
        val kratosApi = KratosApi("http://localhost:4433")

        runBlocking {
            val flow = kratosApi.createLoginFlow()

            val response = flow.completePassword("teamnight", "a5BNzu357S")

            assertTrue { response is LoginFlowStateCompleted }
            assertTrue {
                val state = response as LoginFlowStateCompleted

                state.result.sessionToken.isNotEmpty()
            }
        }
    }

    @Test
    fun testSuccessfulLoginAndWhoami() {
        val kratosApi = KratosApi("http://localhost:4433")

        runBlocking {
            val flow = kratosApi.createLoginFlow()

            val response = flow.completePassword("teamnight", "a5BNzu357S")

            assertTrue { response is LoginFlowStateCompleted }
            assertTrue {
                val state = response as LoginFlowStateCompleted

                state.result.sessionToken.isNotEmpty()
            }

            kratosApi.refreshSessionInfo()
        }
    }
}