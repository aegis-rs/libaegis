package dev.teamnight.aegis.libaegis.crypto.key

import dev.teamnight.aegis.libaegis.crypto.algorithm.HmacSha256

val MessageKeySeed = byteArrayOf(0x01)
val ChainKeySeed = byteArrayOf(0x02)

class ChainKey(
    val bytes: ByteArray
) {
    /**
     * **Use @{DoubleRatchet}**.
     *
     * Generates the next chain key in the ratchet
     *
     * @return next chain key
     */
    fun nextChainKey(): ChainKey {
        val result = HmacSha256.digest(this.bytes, ChainKeySeed)

        return ChainKey(result)
    }

    /**
     * Returns the message keys generated from the chain key.
     */
    val messageKeys: MessageKeys
        get() {
            val result = HmacSha256.digest(this.bytes, MessageKeySeed)

            return MessageKeys(result)
        }
}