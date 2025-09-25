import dev.teamnight.aegis.libaegis.kratos.KratosApi
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class KratosRegistrationFlowTests {

    @Test
    fun testSuccessfulRegistration() {
        val kratosApi = KratosApi("http://localhost:4433")

        runBlocking {
            val flow = kratosApi.createRegistrationFlow()

            val response = flow.complete("a5BNzu357S", "teamnight", "admin@teamnight.dev")

            assertTrue {
                response.sessionToken.isNotEmpty()
            }
        }
    }
}