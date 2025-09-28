package dev.teamnight.aegis.libaegis.crypto.key

import dev.teamnight.aegis.libaegis.crypto.ECDHResult

expect class RootKey(bytes: ByteArray) {
    val bytes: ByteArray
    fun nextRootKey(pair: ECDHResult): Pair<RootKey, ChainKey>

    companion object {
        val INITIAL_ROOT_KEY_INFO: ByteArray
        val RATCHET_INFO: ByteArray

        /**
         * Generates the master secret, which is used to derive the root key and the chain key.
         *
         * Depending which side we are, this is either the sending chain key or the receiving chain key.
         *
         * @param results The 3 or 4 DH results
         *
         * @return The root key
         */
        fun generateInitialRootKey(results: Array<ECDHResult>): Pair<RootKey, ChainKey>
    }
}