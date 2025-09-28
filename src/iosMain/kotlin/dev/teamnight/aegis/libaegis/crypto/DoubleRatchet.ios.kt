package dev.teamnight.aegis.libaegis.crypto

import dev.teamnight.aegis.libaegis.crypto.key.*

actual class DoubleRatchet actual private constructor(
    actual var rootKey: RootKey,
    actual var receivedRatchetKey: RatchetKey?,
    actual var ownRatchetKey: RatchetKey,
    actual var receivingChainKey: ChainKey?,
    actual var sendingChainKey: ChainKey?,
    actual val skippedMessageKeys: MutableMap<Pair<PublicKey, Int>, MessageKeys>
) : AutoCloseable {
    actual var lastSendingChainMessageAmount: Int = 0
    actual var sendingMessageNumber: Int = 0
    actual var receivingMessageNumber: Int = 0

    actual constructor(
        keys: Pair<RootKey, ChainKey>,
        receivedRatchetKey: RatchetKey?
    ) : this(
        keys.first,
        receivedRatchetKey,
        receivingChainKey = null,
        sendingChainKey = null
    ) {
        if (receivedRatchetKey != null) {
            receivingChainKey = keys.second
        } else {
            sendingChainKey = keys.second
        }
    }

    actual fun encrypt(message: ByteArray): Ciphertext {
        TODO("Not yet implemented")
    }

    actual fun decrypt(ciphertext: Ciphertext): ByteArray {
        TODO("Not yet implemented")
    }

    actual override fun close() {
        TODO("Not yet implemented")
    }

    actual companion object {
        actual fun fromExisting(
            rootKey: RootKey,
            receivedRatchetKey: RatchetKey,
            ownRatchetKey: RatchetKey,
            receivingChainKey: ChainKey,
            sendingChainKey: ChainKey,
            lastSendingChainMessageAmount: Int,
            receivingMessageNumber: Int,
            sendingMessageNumber: Int,
            skippedMessageKeys: MutableMap<Pair<PublicKey, Int>, MessageKeys>
        ): DoubleRatchet {
            TODO("Not yet implemented")
        }
    }
}

actual class Header actual constructor(
    dhPublicKey: ByteArray,
    lastSendingChainMessageAmount: Int,
    messageNumber: Int
) {
    actual val dhPublicKey: ByteArray
        get() = TODO("Not yet implemented")
    actual val lastSendingChainMessageAmount: Int
        get() = TODO("Not yet implemented")
    actual val messageNumber: Int
        get() = TODO("Not yet implemented")

    actual fun toBytes(): ByteArray {
        TODO("Not yet implemented")
    }

    actual companion object {
        actual val MIN_HEADER_LENGTH: Int
            get() = TODO("Not yet implemented")

        actual fun fromBytes(bytes: ByteArray): Header {
            TODO("Not yet implemented")
        }
    }
}