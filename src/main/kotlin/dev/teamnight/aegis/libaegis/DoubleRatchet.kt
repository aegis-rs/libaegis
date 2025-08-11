package dev.teamnight.aegis.libaegis

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.KeyParameter
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.KeyAgreement

class DoubleRatchet private constructor(
    var rootKey: RootKey,
    val receivedRatchetKey: RatchetKey? = null,
    var receivingChainKey: ChainKey? = null,
    var sendingChainKey: ChainKey? = null
) {
    val ownRatchetKey: RatchetKey

    init {
        val kpg = KeyPairGenerator.getInstance("X25519")
        val pair = kpg.generateKeyPair()

        ownRatchetKey = RatchetKey(pair.public, pair.private)
    }

    constructor(keys: Pair<RootKey, ChainKey>,
                receivedRatchetKey: RatchetKey?
    ) : this(
        keys.first,
        receivedRatchetKey,
        null,
        null
    ) {
        if(receivedRatchetKey != null) {
            receivingChainKey = keys.second

            val pair = rootKey.nextRootKey(ECDHResult(ownRatchetKey.privateKey!!, receivedRatchetKey.publicKey!!))

            rootKey = pair.first
            sendingChainKey = pair.second
        } else {
            sendingChainKey = keys.second
        }
    }

    fun createNextReceivingChainKey(): ChainKey? {
        return this.receivingChainKey?.nextChainKey()
    }

    fun createNextSendingChainKey(): ChainKey? {
        return this.sendingChainKey?.nextChainKey()
    }
}

class HKDF {
    companion object {
        fun extract(salt: ByteArray, ikm: ByteArray): ByteArray {
            val hkdf = HKDFBytesGenerator(SHA256Digest())
            val params = HKDFParameters(ikm, salt, null)
            hkdf.init(params)

            val prk = ByteArray(32)
            hkdf.generateBytes(prk, 0, 32)

            return prk
        }

        fun expand(prk: ByteArray, info: ByteArray, length: Int): ByteArray {
            val hkdf = HKDFBytesGenerator(SHA256Digest())
            val params = HKDFParameters(null, prk, info)
            hkdf.init(params)

            val result = ByteArray(length)
            hkdf.generateBytes(result, 0, length)

            return result
        }
    }
}

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
 * The
 */
data class RatchetKey(
    val publicKey: PublicKey? = null,
    val privateKey: PrivateKey? = null
)

class ECDHResult(val privateKey: PrivateKey, val publicKey: PublicKey) {
    val arrayResult: ByteArray

    init {
        val ka = KeyAgreement.getInstance("X25519")
        ka.init(privateKey)
        ka.doPhase(publicKey, true)

        this.arrayResult = ka.generateSecret()
    }
}

class RootKey(
    val bytes: ByteArray,
) {
    fun nextRootKey(pair: ECDHResult): Pair<RootKey, ChainKey>  {
        val prk = HKDF.extract(bytes, pair.arrayResult)

        val combinedKeys = HKDF.expand(prk, RATCHET_INFO, 64)
        val rootKey = combinedKeys.copyOfRange(0, 32)
        val chainKey = combinedKeys.copyOfRange(32, 64)

        return RootKey(rootKey) to ChainKey(chainKey)
    }

    companion object {
        val MASTER_SECRET_SALT: ByteArray = byteArrayOf(0x00)
        val INITIAL_ROOT_KEY_INFO   = "AegisRootKey".toByteArray()

        val RATCHET_INFO = "AegisRatchet".toByteArray()

        /**
         * Generates the master secret, which is used to derive the root key and the chain key.
         *
         * Depending which side we are, this is either the sending chain key or the receiving chain key.
         *
         * @param results The 3 or 4 DH results
         *
         * @return The root key
         */
        fun generateInitialRootKey(results: Array<ECDHResult>): Pair<RootKey, ChainKey> {
            val len = results.sumOf { it.arrayResult.size }

            //Concatenate all the DH results
            val result = ByteArray(len)
            var offset = 0
            for (res in results) {
                res.arrayResult.copyInto(result, offset)
                offset += res.arrayResult.size
            }

            //Get Master Secret from HKDF
            val masterSecret = HKDF.extract(MASTER_SECRET_SALT, result)

            //Generate initial root key and chain key
            val combinedKeys = HKDF.expand(masterSecret, INITIAL_ROOT_KEY_INFO, 64)
            val rootKey = combinedKeys.copyOfRange(0, 32)
            val chainKey = combinedKeys.copyOfRange(32, 64)

            return RootKey(rootKey) to ChainKey(chainKey)
        }
    }
}

val MessageKeySeed = byteArrayOf(0x01)
val ChainKeySeed = byteArrayOf(0x02)

class ChainKey(
    val bytes: ByteArray
) {
    fun nextChainKey(): ChainKey {
        val hmac = HMac(SHA256Digest())
        hmac.init(KeyParameter(this.bytes))
        hmac.update(ChainKeySeed, 0, ChainKeySeed.size)

        val result = ByteArray(32)
        hmac.doFinal(result, 0)

        return ChainKey(result)
    }

    val messageKey: MessageKey
        get() {
            val hmac = HMac(SHA256Digest())
            hmac.init(KeyParameter(this.bytes))
            hmac.update(MessageKeySeed, 0, MessageKeySeed.size)

            val result = ByteArray(32)
            hmac.doFinal(result, 0)

            return MessageKey(result)
        }
}

class MessageKey(val bytes: ByteArray) {
    val cipherKey: ByteArray
    val macKey: ByteArray
    val iv: ByteArray

    init {
        val prk = HKDF.extract(this.bytes, ByteArray(32))
        val result = HKDF.expand(prk, "AegisMessageKey".toByteArray(), 80)

        this.cipherKey = result.copyOfRange(0, 32)
        this.macKey = result.copyOfRange(32, 48)
        this.iv = result.copyOfRange(48, 64)
    }
}