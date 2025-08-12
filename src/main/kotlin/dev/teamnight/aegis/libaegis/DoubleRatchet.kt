package dev.teamnight.aegis.libaegis

import dev.teamnight.aegis.libaegis.crypto.ECDHResult
import dev.teamnight.aegis.libaegis.key.*
import java.nio.ByteBuffer
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.Destroyable

const val MAX_SKIP = 1000
const val MAX_SKIPPED_MESSAGES = 10000

class DoubleRatchet private constructor(
    var rootKey: RootKey,
    var receivedRatchetKey: RatchetKey? = null,
    var ownRatchetKey: RatchetKey = RatchetKey.generate(),
    var receivingChainKey: ChainKey? = null,
    var sendingChainKey: ChainKey? = null,
    val skippedMessageKeys: MutableMap<Pair<PublicKey, Int>, MessageKeys> = mutableMapOf()
) : AutoCloseable, Destroyable {
    var lastSendingChainMessageAmount: Int = 0
    var sendingMessageNumber: Int = 0
    var receivingMessageNumber: Int = 0

    /**
     * Creates a new double ratchet state from the given keys.
     *
     * **Do not use this constructor when trying to reinstantiate an already established double ratchet state, this
     * will result in an invalid state on the other side! Instead, use [DoubleRatchet.fromExisting]**
     *
     * If the received ratchet key is not null, the chain key provided will be treated as the receiving chain key,
     * otherwise it will be treated as the sending chain key.
     *
     * @param keys The root key and the chain key to use
     * @param receivedRatchetKey The received ratchet key
     * @param ownRatchetKey The own ratchet key
     */
    constructor(
        keys: Pair<RootKey, ChainKey>,
        receivedRatchetKey: RatchetKey?
    ) : this(
        keys.first,
        receivedRatchetKey,
        receivingChainKey = null,
        sendingChainKey = null
    ) {
        if(receivedRatchetKey != null) {
            receivingChainKey = keys.second
        } else {
            sendingChainKey = keys.second
        }
    }

    /**
     * Encrypts the given message using the sending chain key.
     *
     * @param message The message to encrypt
     * @return The encrypted message
     */
    fun encrypt(message: ByteArray): Ciphertext {
        if (sendingChainKey == null) {
            //Late init to be able to read the first messages using the initial root key
            requireNotNull(receivedRatchetKey)
            { "SendingChainKey or ReceivedRatchetKey must be initialized before encrypting" }

            val pair = rootKey.nextRootKey(ECDHResult(ownRatchetKey.privateKey!!, receivedRatchetKey!!.publicKey))

            rootKey = pair.first
            sendingChainKey = pair.second
        }

        val chainKey = sendingChainKey ?: throw IllegalStateException("Cannot encrypt without sending chain key")

        this.sendingChainKey = chainKey.nextChainKey()

        val header = Header(ownRatchetKey.publicKey.raw, lastSendingChainMessageAmount, sendingMessageNumber)
        val headerBytes = header.toBytes()

        val messageKeys = chainKey.messageKeys

        sendingMessageNumber++

        val cipher = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding")
        val spec = SecretKeySpec(messageKeys.cipherKey, "ChaCha20")
        val paramSpec = IvParameterSpec(messageKeys.iv)
        cipher.init(Cipher.ENCRYPT_MODE, spec, paramSpec)
        cipher.updateAAD(headerBytes)

        return Ciphertext(cipher.doFinal(message), headerBytes)
    }

    /**
     * Decrypts the given ciphertext using the receiving chain key.
     *
     * This method might perform a ratchet step in case the receiving DH ratchet key does not match the saved one.
     *
     * @param ciphertext The ciphertext to decrypt
     * @return The decrypted message
     *
     * @throws javax.crypto.AEADBadTagException if decryption fails due to an invalid tag, you should renew the root key
     */
    fun decrypt(ciphertext: Ciphertext): ByteArray {
        val header = Header.fromBytes(ciphertext.headerBytes)
        val headerPublicKey = publicKeyFromRaw(header.dhPublicKey)

        val skippedPair = Pair(headerPublicKey, header.messageNumber)

        if(skippedMessageKeys.containsKey(skippedPair)) {
            val skippedMessageKeys = skippedMessageKeys[skippedPair]!!
            this.skippedMessageKeys.remove(skippedPair)

            return doDecrypt(ciphertext, skippedMessageKeys)
        }

        if(!headerPublicKey.encoded.contentEquals(receivedRatchetKey?.publicKey?.encoded)) {
            skipMessages(header.lastSendingChainMessageAmount)
            doRatchet(RatchetKey(headerPublicKey))
        }

        //Skip to message number of this ciphertext
        skipMessages(header.messageNumber)

        receivingMessageNumber++

        //Update chain key
        val chainKey = receivingChainKey ?: throw IllegalStateException("Cannot decrypt without receiving chain key")
        val messageKeys = chainKey.messageKeys

        this.receivingChainKey = chainKey.nextChainKey()

        return doDecrypt(ciphertext, messageKeys)
    }

    private fun skipMessages(until: Int) {
        require(Int.MAX_VALUE - MAX_SKIP > receivingMessageNumber) { "Skip exceeds Int.MAX_VALUE" }

        if (receivingMessageNumber + MAX_SKIP < until) {
            throw IllegalArgumentException("Cannot skip more than $MAX_SKIP messages")
        }

        while(receivingMessageNumber < until) {
            val chainKey = receivingChainKey
                ?: throw IllegalStateException("Cannot decrypt without receiving chain key")

            requireNotNull(receivedRatchetKey) { "Cannot skip messages without a received ratchet key" }

            this.receivingChainKey = chainKey.nextChainKey()

            if (skippedMessageKeys.size >= MAX_SKIPPED_MESSAGES) {
                throw IllegalStateException("Skipped message keys map exceeds maximum size")
            }

            this.skippedMessageKeys[Pair(receivedRatchetKey!!.publicKey, receivingMessageNumber)] = chainKey.messageKeys
            this.receivingMessageNumber++
        }
    }

    private fun doDecrypt(ciphertext: Ciphertext, keys: MessageKeys): ByteArray {
        val cipher = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding")
        val spec = SecretKeySpec(keys.cipherKey, "ChaCha20")
        val paramSpec = IvParameterSpec(keys.iv)

        cipher.init(Cipher.DECRYPT_MODE, spec, paramSpec)
        cipher.updateAAD(ciphertext.headerBytes)

        return cipher.doFinal(ciphertext.bytes)
    }

    private fun doRatchet(receivedKey: RatchetKey) {
        this.lastSendingChainMessageAmount = this.sendingMessageNumber
        this.sendingMessageNumber = 0
        this.receivingMessageNumber = 0

        this.receivedRatchetKey = receivedKey

        val ecdh = ECDHResult(ownRatchetKey.privateKey!!, receivedKey.publicKey)
        val pair = rootKey.nextRootKey(ecdh)

        this.rootKey = pair.first
        this.receivingChainKey = pair.second

        //Generate new DH Ratchet key
        this.ownRatchetKey = RatchetKey.generate()

        val ecdh2 = ECDHResult(ownRatchetKey.privateKey!!, receivedKey.publicKey)
        val pair2 = rootKey.nextRootKey(ecdh2)

        this.rootKey = pair2.first
        this.sendingChainKey = pair2.second
    }

    override fun close() = this.destroy()

    override fun destroy() {
        this.rootKey.bytes.fill(0)
        this.receivingChainKey?.bytes?.fill(0)
        this.sendingChainKey?.bytes?.fill(0)
        this.receivedRatchetKey?.privateKey?.destroy()
        this.ownRatchetKey.privateKey?.destroy()
        this.skippedMessageKeys.clear()
    }

    companion object {
        /**
         * Initializes a double ratchet state from an existing one.
         */
        fun fromExisting(
            rootKey: RootKey,
            receivedRatchetKey: RatchetKey,
            ownRatchetKey: RatchetKey,
            receivingChainKey: ChainKey,
            sendingChainKey: ChainKey,
            lastSendingChainMessageAmount: Int,
            receivingMessageNumber: Int,
            sendingMessageNumber: Int,
            skippedMessageKeys: MutableMap<Pair<PublicKey, Int>, MessageKeys>
        ): DoubleRatchet {
            val dh = DoubleRatchet(
                rootKey,
                receivedRatchetKey,
                ownRatchetKey,
                receivingChainKey,
                sendingChainKey,
                skippedMessageKeys
            )

            dh.lastSendingChainMessageAmount = lastSendingChainMessageAmount
            dh.receivingMessageNumber = receivingMessageNumber
            dh.sendingMessageNumber = sendingMessageNumber

            return dh
        }
    }
}

class Ciphertext(val bytes: ByteArray, val headerBytes: ByteArray)

/**
 * Double ratchet header
 */
class Header(
    val dhPublicKey: ByteArray,
    val lastSendingChainMessageAmount: Int,
    val messageNumber: Int,
) {
    fun toBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(dhPublicKey.size + 8)

        buffer.put(dhPublicKey)
        buffer.putInt(lastSendingChainMessageAmount)
        buffer.putInt(messageNumber)

        return buffer.array()
    }

    companion object {
        const val MIN_HEADER_LENGTH = 32 + 4 + 4

        fun fromBytes(bytes: ByteArray): Header {
            require(bytes.size >= MIN_HEADER_LENGTH) { "Invalid header length" }
            val buffer = ByteBuffer.wrap(bytes)

            val dhPublicKey = ByteArray(32)
            buffer.get(dhPublicKey)
            val lastSendingChainMessageAmount = buffer.int
            val messageNumber = buffer.int

            require(messageNumber >= 0) { "Message number cannot be negative" }
            require(lastSendingChainMessageAmount >= 0) { "Last sending chain message amount cannot be negative" }

            return Header(dhPublicKey, lastSendingChainMessageAmount, messageNumber)
        }
    }
}