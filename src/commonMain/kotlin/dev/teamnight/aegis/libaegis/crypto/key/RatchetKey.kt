package dev.teamnight.aegis.libaegis.crypto.key

import dev.teamnight.aegis.libaegis.crypto.algorithm.X25519

class RatchetKey(
    val publicKey: PublicKey,
    val privateKey: PrivateKey? = null
) {
    companion object {
        fun generate(): RatchetKey {
            val pair = X25519.generateKeyPair()

            return RatchetKey(pair.publicKey, pair.privateKey)
        }
    }
}