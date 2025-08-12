package dev.teamnight.aegis.libaegis.key

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.KeyParameter

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
        val hmac = HMac(SHA256Digest())
        hmac.init(KeyParameter(this.bytes))
        hmac.update(ChainKeySeed, 0, ChainKeySeed.size)

        val result = ByteArray(32)
        hmac.doFinal(result, 0)

        return ChainKey(result)
    }

    /**
     * Returns the message keys generated from the chain key.
     */
    val messageKeys: MessageKeys
        get() {
            val hmac = HMac(SHA256Digest())
            hmac.init(KeyParameter(this.bytes))
            hmac.update(MessageKeySeed, 0, MessageKeySeed.size)

            val result = ByteArray(32)
            hmac.doFinal(result, 0)

            return MessageKeys(result)
        }
}