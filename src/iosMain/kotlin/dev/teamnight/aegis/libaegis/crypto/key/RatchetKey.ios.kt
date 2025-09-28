package dev.teamnight.aegis.libaegis.crypto.key

actual class RatchetKey actual constructor(
    publicKey: PublicKey,
    privateKey: PrivateKey?
) {
    actual val publicKey: PublicKey
        get() = TODO("Not yet implemented")
    actual val privateKey: PrivateKey?
        get() = TODO("Not yet implemented")

    actual companion object {
        actual fun generate(): RatchetKey {
            TODO("Not yet implemented")
        }
    }
}