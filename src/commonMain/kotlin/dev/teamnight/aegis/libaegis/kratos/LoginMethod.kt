package dev.teamnight.aegis.libaegis.kratos

enum class LoginMethod {
    PASSWORD,
    OIDC,
    TOTP,
    LOOKUP_SECRET,
    WEBAUTHN,
    CODE,
    PASSKEY,
    PROFILE,
    LINK_RECOVERY,
    CODE_RECOVERY
}