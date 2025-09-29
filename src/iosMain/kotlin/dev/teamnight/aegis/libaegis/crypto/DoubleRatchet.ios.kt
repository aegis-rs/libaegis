package dev.teamnight.aegis.libaegis.crypto

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