package dev.teamnight.aegis.libaegis.crypto.algorithm

import dev.teamnight.aegis.libaegis.crypto.key.JavaPrivateKey
import dev.teamnight.aegis.libaegis.crypto.key.JavaPublicKey
import dev.teamnight.aegis.libaegis.crypto.key.KeyPair
import java.security.KeyPairGenerator

actual class X25519 {
    actual companion object {
        actual fun generateKeyPair(): KeyPair {
            val kpg = KeyPairGenerator.getInstance("X25519")
            val pair = kpg.generateKeyPair()

            return KeyPair(JavaPublicKey(pair.public), JavaPrivateKey(pair.private))
        }
    }
}