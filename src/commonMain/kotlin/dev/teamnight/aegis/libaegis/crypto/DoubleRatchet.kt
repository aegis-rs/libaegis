package dev.teamnight.aegis.libaegis.crypto

import dev.teamnight.aegis.libaegis.crypto.algorithm.ChaCha20Poly1305
import dev.teamnight.aegis.libaegis.crypto.algorithm.ECDHResult
import dev.teamnight.aegis.libaegis.crypto.key.*

const val MAX_SKIP = 1000
const val MAX_SKIPPED_MESSAGES = 10000

class DoubleRatchet private constructor(
    var rootKey: RootKey,
    var receivedRatchetKey: RatchetKey? = null,
    var ownRatchetKey: RatchetKey = RatchetKey.generate(),
    var receivingChainKey: ChainKey? = null,
    var sendingChainKey: ChainKey? = null,
    val skippedMessageKeys: MutableMap<Pair<PublicKey, Int>, MessageKeys> = mutableMapOf()
) : AutoCloseable {

    var lastSendingChainMessageAmount: Int = 0
    var sendingMessageNumber: Int = 0
    var receivingMessageNumber: Int = 0

    /**
     * Creates a new double ratchet state from the given keys.
     *
     * When starting a Double Ratchet with a partner, one side needs to start the exchange of the secret key, request
     * a ratchet key and generate a random ratchet key. If you are this side, use this constructor. If you are the side,
     * that received a generated ratchet key, then use the other constructor.
     *
     * For example, if you exchange the shared secret using X3DH and you are the side that uses their pre-key bundle,
     * then you would use this constructor. This constructor will generate the ratchet key for you and use the ratchet
     * key from the pre-key bundle, e.g. the Server Signed Pre-Key.
     *
     * This constructor will put you in the state to send and receive messages.
     *
     * **Do not use this constructor when trying to reinstantiate an already established double ratchet state, this
     * will result in an invalid state on the other side! Instead, use [DoubleRatchet.fromExisting]**
     *
     * @param initialRootKey The initial root key
     * @param receivedRatchetKey The received ratchet key
     * @param ownRatchetKey The own ratchet key
     */
    constructor(
        initialRootKey: RootKey,
        receivedRatchetKey: RatchetKey
    ) : this(
        initialRootKey,
        receivedRatchetKey,
        receivingChainKey = null,
        sendingChainKey = null
    ) {
        val keys = initialRootKey.nextRootKey(
            ECDHResult(
                this.ownRatchetKey.privateKey!!,
                receivedRatchetKey.publicKey
            )
        )

        this.rootKey = keys.first
        this.sendingChainKey = keys.second
    }

    /**
     * Creates a new double ratchet state from the given keys.
     *
     * When starting a Double Ratchet with a partner, one side needs to start the exchange of the secret key, request
     * a ratchet key and generate a random ratchet key. If you are the other side, use this constructor.
     *
     * Please only use this constructor when you are the side that pre-published a key bundle containing their ratchet
     * key for this step.
     *
     * This constructor will put you in the state to send and receive messages.
     *
     * **Do not use this constructor when trying to reinstantiate an already established double ratchet state, this
     * will result in an invalid state on the other side! Instead, use [DoubleRatchet.fromExisting]**
     *
     * @see [DoubleRatchet]
     */
    constructor(
        initialRootKey: RootKey,
        receivedRatchetKey: RatchetKey,
        ownRatchetKey: RatchetKey
    ) : this(
        initialRootKey,
        receivedRatchetKey,
        ownRatchetKey,
        receivingChainKey = null,
        sendingChainKey = null
    ) {
        doRatchet(receivedRatchetKey)
    }

    /**
     * Encrypts the given message using the sending chain key.
     *
     * @param message The message to encrypt
     * @return The encrypted message
     */
    fun encrypt(message: ByteArray): Ciphertext {
        requireNotNull(sendingChainKey) { "SendingChainKey must be initialized before encrypting" }

        val chainKey = sendingChainKey ?: throw IllegalStateException("Cannot encrypt without sending chain key")

        this.sendingChainKey = chainKey.nextChainKey()

        val header =
            Header(ownRatchetKey.publicKey.raw, lastSendingChainMessageAmount, sendingMessageNumber)
        val headerBytes = header.toBytes()

        val messageKeys = chainKey.messageKeys

        sendingMessageNumber++

        return Ciphertext(
            ChaCha20Poly1305.encrypt(messageKeys.cipherKey, messageKeys.iv, message, headerBytes),
            headerBytes
        )
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
        val headerPublicKey = PublicKey.fromRaw(header.dhPublicKey)

        val skippedPair = Pair(headerPublicKey, header.messageNumber)

        if (skippedMessageKeys.containsKey(skippedPair)) {
            val skippedMessageKeys = skippedMessageKeys[skippedPair]!!
            this.skippedMessageKeys.remove(skippedPair)

            return ChaCha20Poly1305.decrypt(
                skippedMessageKeys.cipherKey,
                skippedMessageKeys.iv,
                ciphertext.bytes,
                ciphertext.headerBytes
            )
        }

        if (!headerPublicKey.encoded.contentEquals(receivedRatchetKey?.publicKey?.encoded)) {
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

        return ChaCha20Poly1305.decrypt(
            messageKeys.cipherKey,
            messageKeys.iv,
            ciphertext.bytes,
            ciphertext.headerBytes
        )
    }

    private fun skipMessages(until: Int) {
        require(Int.MAX_VALUE - MAX_SKIP > receivingMessageNumber) { "Skip exceeds Int.MAX_VALUE" }

        if (receivingMessageNumber + MAX_SKIP < until) {
            throw IllegalArgumentException("Cannot skip more than $MAX_SKIP messages")
        }

        while (receivingMessageNumber < until) {
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

    fun destroy() {
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
expect class Header(
    dhPublicKey: ByteArray,
    lastSendingChainMessageAmount: Int,
    messageNumber: Int,
) {
    val dhPublicKey: ByteArray
    val lastSendingChainMessageAmount: Int
    val messageNumber: Int

    fun toBytes(): ByteArray

    companion object {
        val MIN_HEADER_LENGTH: Int

        fun fromBytes(bytes: ByteArray): Header
    }
}