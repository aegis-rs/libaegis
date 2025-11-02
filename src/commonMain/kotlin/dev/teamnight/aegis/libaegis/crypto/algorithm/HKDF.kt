package dev.teamnight.aegis.libaegis.crypto.algorithm

expect class HKDF {
    companion object {
        fun extract(salt: ByteArray, ikm: ByteArray): ByteArray
        fun expand(prk: ByteArray, info: ByteArray, length: Int): ByteArray

        fun generateOutput(salt: ByteArray, ikm: ByteArray, info: ByteArray, length: Int): ByteArray
    }
}