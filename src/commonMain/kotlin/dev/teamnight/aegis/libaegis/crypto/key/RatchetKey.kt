package dev.teamnight.aegis.libaegis.crypto.key

expect class RatchetKey(
    publicKey: PublicKey,
    privateKey: PrivateKey? = null
) {
    val publicKey: PublicKey
    val privateKey: PrivateKey?

    companion object {
        fun generate(): RatchetKey
    }
}