package dev.teamnight.aegis.libaegis.crypto.key

import dev.teamnight.aegis.libaegis.crypto.ECDHResult
import dev.teamnight.aegis.libaegis.crypto.HKDF
import dev.teamnight.aegis.libaegis.crypto.key.RootKey.Companion.INITIAL_ROOT_KEY_INFO
import java.security.PrivateKey
import java.security.PublicKey

/**
 * The X25519 identity key pair for usage in X3DH.
 *
 * @property publicKey The public key
 * @property privateKey The private key
 */
data class IdentityKeyPair(
    val publicKey: PublicKey,
    val privateKey: PrivateKey? = null
)

/**
 * The X25519 signed pre-key pair for usage in X3DH.
 *
 * @property publicKey The public key
 * @property signature The signature
 * @property privateKey The private key
 */
data class SignedPreKey(
    val publicKey: PublicKey,
    val signature: ByteArray,
    val privateKey: PrivateKey? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SignedPreKey

        if (publicKey != other.publicKey) return false
        if (!signature.contentEquals(other.signature)) return false
        if (privateKey != other.privateKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + signature.contentHashCode()
        result = 31 * result + (privateKey?.hashCode() ?: 0)
        return result
    }
}

/**
 * The X25519 one-time pre-key pair for usage in X3DH.
 *
 * @property publicKey The public key
 * @property privateKey The private key
 */
data class OnetimePreKey(
    val publicKey: PublicKey,
    val privateKey: PrivateKey? = null
)

/**
 * Generates a shared secret to use in ciphers.
 *
 * @return Pair containing shared secret and iv for ChaCha20-Poly1305
 */
fun generateSecret(results: Array<ECDHResult>, info: String): Pair<ByteArray, ByteArray> {
    val len = results.sumOf { it.arrayResult.size }

    //Concatenate all the DH results
    val result = ByteArray(len)
    var offset = 0
    for (res in results) {
        res.arrayResult.copyInto(result, offset)
        offset += res.arrayResult.size
    }

    //Get Master Secret from HKDF
    val masterSecret = HKDF.Companion.extract(ByteArray(32), result)

    //Generate initial root key and chain key
    val key = HKDF.Companion.expand(masterSecret, INITIAL_ROOT_KEY_INFO, 44)

    return key.copyOfRange(0, 32) to key.copyOfRange(32, 44)
}