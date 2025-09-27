package dev.teamnight.aegis.libaegis.kratos.http

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

open class LoginRequest(
    val method: String,
)

class PasswordLoginRequest(
    val password: String,
    val identifier: String
) : LoginRequest("password")

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class TotpLoginRequest(
    val totpCode: String,
) : LoginRequest("totp")

class CodeLoginRequest(
    val code: String,
    val identifier: String,
    val resend: Boolean = false,
) : LoginRequest("password")