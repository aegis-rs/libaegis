package dev.teamnight.aegis.libaegis.crypto

expect class HKDF {
    companion object {
        fun extract(salt: ByteArray, ikm: ByteArray): ByteArray
        fun expand(prk: ByteArray, info: ByteArray, length: Int): ByteArray
    }
}