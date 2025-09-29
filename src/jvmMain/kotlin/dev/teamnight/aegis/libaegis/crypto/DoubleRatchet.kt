package dev.teamnight.aegis.libaegis.crypto

import java.nio.ByteBuffer

/**
 * Double ratchet header
 */
actual class Header actual constructor(
    actual val dhPublicKey: ByteArray,
    actual val lastSendingChainMessageAmount: Int,
    actual val messageNumber: Int,
) {
    actual fun toBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(dhPublicKey.size + 8)

        buffer.put(dhPublicKey)
        buffer.putInt(lastSendingChainMessageAmount)
        buffer.putInt(messageNumber)

        return buffer.array()
    }

    actual companion object {
        actual const val MIN_HEADER_LENGTH = 32 + 4 + 4

        actual fun fromBytes(bytes: ByteArray): Header {
            require(bytes.size >= MIN_HEADER_LENGTH) { "Invalid header length" }
            val buffer = ByteBuffer.wrap(bytes)

            val dhPublicKey = ByteArray(32)
            buffer.get(dhPublicKey)
            val lastSendingChainMessageAmount = buffer.int
            val messageNumber = buffer.int

            require(messageNumber >= 0) { "Message number cannot be negative" }
            require(lastSendingChainMessageAmount >= 0) { "Last sending chain message amount cannot be negative" }

            return Header(dhPublicKey, lastSendingChainMessageAmount, messageNumber)
        }
    }
}