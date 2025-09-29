package dev.teamnight.aegis.libaegis.crypto.key

actual interface Key
actual interface PublicKey : Key {
    actual val raw: ByteArray
    actual val encoded: ByteArray

    actual companion object {
        actual fun fromRaw(raw: ByteArray): PublicKey {
            TODO("Not yet implemented")
        }
    }
}

actual interface PrivateKey : Key {
    actual fun destroy()
}