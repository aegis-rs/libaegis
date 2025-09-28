import dev.teamnight.aegis.libaegis.kratos.KratosApi
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class KratosTests {

    @Test
    fun testSuccessfulRegistration() {
        val kratosApi = KratosApi("http://localhost:4433")

        runBlocking {
            val flow = kratosApi.createRegistrationFlow()

            flow.update("teamnight", "admin@teamnight.dev")

            val response = flow.complete("a5BNzu357S")

            assertTrue {
                response.sessionToken.isNotEmpty()
            }
        }
    }

    @Test
    fun testSuccessfulLogin() {
        val kratosApi = KratosApi("http://localhost:4433")

        runBlocking {
            val flow = kratosApi.createLoginFlow()

            val response = flow.completePassword("teamnight", "a5BNzu357S")

            assertTrue {
                response.sessionToken.isNotEmpty()
            }
        }
    }
}