package dev.teamnight.aegis.libaegis.crypto.algorithm

expect class ChaCha20Poly1305 {
    companion object {
        fun encrypt(key: ByteArray, iv: ByteArray, data: ByteArray, associatedData: ByteArray): ByteArray

        fun decrypt(key: ByteArray, iv: ByteArray, data: ByteArray, associatedData: ByteArray): ByteArray
    }
}