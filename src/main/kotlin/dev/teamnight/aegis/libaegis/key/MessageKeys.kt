package dev.teamnight.aegis.libaegis.key

import dev.teamnight.aegis.libaegis.crypto.HKDF

/**
 * Message Keys for usage in encryption algorithms.
 *
 * You can use this to initialize a cipher like ChaCha20-Poly1305.
 *
 * Example:
 *
 *
 * @property cipherKey Secret key for cipher
 * @property macKey Secret key for MAC
 * @property iv Initialization vector
 */
class MessageKeys(val bytes: ByteArray) {
    val cipherKey: ByteArray
    val macKey: ByteArray
    val iv: ByteArray

    init {
        val prk = HKDF.Companion.extract(ByteArray(32), this.bytes)
        val result = HKDF.Companion.expand(prk, "AegisMessageKey".toByteArray(), 72)

        this.cipherKey = result.copyOfRange(0, 32)
        this.macKey = result.copyOfRange(32, 64)
        this.iv = result.copyOfRange(64, 72)
    }
}