package dev.teamnight.aegis.libaegis.crypto.algorithm

actual class ChaCha20Poly1305 {
    actual companion object {
        actual fun encrypt(
            key: ByteArray,
            iv: ByteArray,
            data: ByteArray,
            associatedData: ByteArray
        ): ByteArray {
            TODO("Not yet implemented")
        }

        actual fun decrypt(
            key: ByteArray,
            iv: ByteArray,
            data: ByteArray,
            associatedData: ByteArray
        ): ByteArray {
            TODO("Not yet implemented")
        }
    }
}