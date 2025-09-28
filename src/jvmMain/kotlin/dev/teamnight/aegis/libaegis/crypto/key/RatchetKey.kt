package dev.teamnight.aegis.libaegis.crypto.key

import java.security.KeyPairGenerator

actual data class RatchetKey actual constructor(
    actual val publicKey: PublicKey,
    actual val privateKey: PrivateKey?
) {
    actual companion object {
        actual fun generate(): RatchetKey {
            val kpg = KeyPairGenerator.getInstance("X25519")
            val pair = kpg.generateKeyPair()

            return RatchetKey(JavaPublicKey(pair.public), JavaPrivateKey(pair.private))
        }
    }
}