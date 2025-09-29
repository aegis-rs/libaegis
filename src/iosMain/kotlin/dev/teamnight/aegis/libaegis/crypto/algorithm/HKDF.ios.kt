package dev.teamnight.aegis.libaegis.crypto.algorithm

actual class HKDF {
    actual companion object {
        actual fun extract(salt: ByteArray, ikm: ByteArray): ByteArray {
            TODO("Not yet implemented")
        }

        actual fun expand(prk: ByteArray, info: ByteArray, length: Int): ByteArray {
            TODO("Not yet implemented")
        }
    }
}