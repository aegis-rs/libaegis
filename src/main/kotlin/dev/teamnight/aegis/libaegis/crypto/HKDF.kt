package dev.teamnight.aegis.libaegis.crypto

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters

class HKDF {
    companion object {
        fun extract(salt: ByteArray, ikm: ByteArray): ByteArray {
            val hkdf = HKDFBytesGenerator(SHA256Digest())
            val prk = hkdf.extractPRK(salt, ikm)

            return prk
        }

        fun expand(prk: ByteArray, info: ByteArray, length: Int): ByteArray {
            val hkdf = HKDFBytesGenerator(SHA256Digest())
            val params = HKDFParameters.skipExtractParameters(prk, info)
            hkdf.init(params)

            val result = ByteArray(length)
            hkdf.generateBytes(result, 0, length)

            return result
        }
    }
}