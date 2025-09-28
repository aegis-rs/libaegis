package dev.teamnight.aegis.libaegis.kratos.http

import kotlinx.serialization.Serializable


@Serializable
open class LoginRequest(
    val method: String,
)

@Serializable
class PasswordLoginRequest(
    val password: String,
    val identifier: String
) : LoginRequest("password")

@Serializable
class TotpLoginRequest(
    val totpCode: String,
) : LoginRequest("totp")

@Serializable
class CodeLoginRequest(
    val code: String,
    val identifier: String,
    val resend: Boolean = false,
) : LoginRequest("password")