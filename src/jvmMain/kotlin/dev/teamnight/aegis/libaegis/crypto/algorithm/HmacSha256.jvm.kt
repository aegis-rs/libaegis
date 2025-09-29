package dev.teamnight.aegis.libaegis.crypto.algorithm

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.KeyParameter

actual class HmacSha256 {
    actual companion object {
        actual fun digest(key: ByteArray, data: ByteArray): ByteArray {
            val hmac = HMac(SHA256Digest())
            hmac.init(KeyParameter(key))
            hmac.update(data, 0, data.size)

            val result = ByteArray(32)
            hmac.doFinal(result, 0)

            return result
        }
    }
}