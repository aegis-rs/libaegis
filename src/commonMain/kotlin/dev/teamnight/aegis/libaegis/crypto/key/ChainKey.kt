package dev.teamnight.aegis.libaegis.crypto.key

expect class ChainKey(bytes: ByteArray) {
    val bytes: ByteArray

    fun nextChainKey(): ChainKey

    val messageKeys: MessageKeys
}