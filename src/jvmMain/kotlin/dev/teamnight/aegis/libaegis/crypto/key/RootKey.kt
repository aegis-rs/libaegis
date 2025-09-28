package dev.teamnight.aegis.libaegis.crypto.key

import dev.teamnight.aegis.libaegis.crypto.ECDHResult
import dev.teamnight.aegis.libaegis.crypto.HKDF

actual class RootKey actual constructor(
    actual val bytes: ByteArray,
) {
    actual fun nextRootKey(pair: ECDHResult): Pair<RootKey, ChainKey> {
        val prk = HKDF.extract(bytes, pair.arrayResult)

        val combinedKeys = HKDF.expand(prk, RATCHET_INFO, 64)
        val rootKey = combinedKeys.copyOfRange(0, 32)
        val chainKey = combinedKeys.copyOfRange(32, 64)

        return RootKey(rootKey) to ChainKey(chainKey)
    }

    actual companion object {
        actual val INITIAL_ROOT_KEY_INFO = "AegisRootKey".toByteArray()

        actual val RATCHET_INFO = "AegisRatchet".toByteArray()

        /**
         * Generates the master secret, which is used to derive the root key and the chain key.
         *
         * Depending which side we are, this is either the sending chain key or the receiving chain key.
         *
         * @param results The 3 or 4 DH results
         *
         * @return The root key
         */
        actual fun generateInitialRootKey(results: Array<ECDHResult>): Pair<RootKey, ChainKey> {
            require(results.size == 3 || results.size == 4) { "DH results must contain 3 or 4 elements" }
            val len = results.sumOf { it.arrayResult.size }

            //Concatenate all the DH results
            val result = ByteArray(len)
            var offset = 0
            for (res in results) {
                res.arrayResult.copyInto(result, offset)
                offset += res.arrayResult.size
            }

            //Get Master Secret from HKDF
            val masterSecret = HKDF.extract(ByteArray(32), result)

            //Generate initial root key and chain key
            val combinedKeys = HKDF.expand(masterSecret, INITIAL_ROOT_KEY_INFO, 64)
            val rootKey = combinedKeys.copyOfRange(0, 32)
            val chainKey = combinedKeys.copyOfRange(32, 64)

            return RootKey(rootKey) to ChainKey(chainKey)
        }
    }
}