package dev.teamnight.aegis.libaegis.crypto.key

actual class ChainKey actual constructor(bytes: ByteArray) {
    actual val bytes: ByteArray
        get() = TODO("Not yet implemented")

    actual fun nextChainKey(): ChainKey {
        TODO("Not yet implemented")
    }

    actual val messageKeys: MessageKeys
        get() = TODO("Not yet implemented")
}