package dev.teamnight.aegis.libaegis.crypto.key

import dev.teamnight.aegis.libaegis.crypto.ECDHResult

actual class RootKey actual constructor(bytes: ByteArray) {
    actual val bytes: ByteArray
        get() = TODO("Not yet implemented")

    actual fun nextRootKey(pair: ECDHResult): Pair<RootKey, ChainKey> {
        TODO("Not yet implemented")
    }

    actual companion object {
        actual val INITIAL_ROOT_KEY_INFO: ByteArray
            get() = TODO("Not yet implemented")
        actual val RATCHET_INFO: ByteArray
            get() = TODO("Not yet implemented")

        actual fun generateInitialRootKey(results: Array<ECDHResult>): Pair<RootKey, ChainKey> {
            TODO("Not yet implemented")
        }
    }
}