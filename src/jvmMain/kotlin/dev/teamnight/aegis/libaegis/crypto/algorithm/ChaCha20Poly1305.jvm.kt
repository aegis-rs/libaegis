package dev.teamnight.aegis.libaegis.crypto.algorithm

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

actual class ChaCha20Poly1305 {
    actual companion object {
        actual fun encrypt(
            key: ByteArray,
            iv: ByteArray,
            data: ByteArray,
            associatedData: ByteArray
        ): ByteArray {
            val cipher = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding")
            val spec = SecretKeySpec(key, "ChaCha20")
            val paramSpec = IvParameterSpec(iv)
            cipher.init(Cipher.ENCRYPT_MODE, spec, paramSpec)
            cipher.updateAAD(associatedData)

            return cipher.doFinal(data)
        }

        actual fun decrypt(
            key: ByteArray,
            iv: ByteArray,
            data: ByteArray,
            associatedData: ByteArray
        ): ByteArray {
            val cipher = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding")
            val spec = SecretKeySpec(key, "ChaCha20")
            val paramSpec = IvParameterSpec(iv)

            cipher.init(Cipher.DECRYPT_MODE, spec, paramSpec)
            cipher.updateAAD(associatedData)

            return cipher.doFinal(data)
        }
    }
}