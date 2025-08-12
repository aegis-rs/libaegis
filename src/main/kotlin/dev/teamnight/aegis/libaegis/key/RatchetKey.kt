package dev.teamnight.aegis.libaegis.key

import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey

data class RatchetKey(
    val publicKey: PublicKey? = null,
    val privateKey: PrivateKey? = null
) {
    companion object {
        fun generate(): RatchetKey {
            val kpg = KeyPairGenerator.getInstance("X25519")
            val pair = kpg.generateKeyPair()

            return RatchetKey(pair.public, pair.private)
        }
    }
}