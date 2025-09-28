package dev.teamnight.aegis.libaegis.crypto

import dev.teamnight.aegis.libaegis.crypto.key.*

const val MAX_SKIP = 1000
const val MAX_SKIPPED_MESSAGES = 10000

expect class DoubleRatchet private constructor(
    rootKey: RootKey,
    receivedRatchetKey: RatchetKey? = null,
    ownRatchetKey: RatchetKey = RatchetKey.generate(),
    receivingChainKey: ChainKey? = null,
    sendingChainKey: ChainKey? = null,
    skippedMessageKeys: MutableMap<Pair<PublicKey, Int>, MessageKeys> = mutableMapOf()
) : AutoCloseable {
    var rootKey: RootKey
    var receivedRatchetKey: RatchetKey?
    var ownRatchetKey: RatchetKey
    var receivingChainKey: ChainKey?
    var sendingChainKey: ChainKey?

    val skippedMessageKeys: MutableMap<Pair<PublicKey, Int>, MessageKeys>

    var lastSendingChainMessageAmount: Int
    var sendingMessageNumber: Int
    var receivingMessageNumber: Int

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
    )

    /**
     * Encrypts the given message using the sending chain key.
     *
     * @param message The message to encrypt
     * @return The encrypted message
     */
    fun encrypt(message: ByteArray): Ciphertext

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
    fun decrypt(ciphertext: Ciphertext): ByteArray

    override fun close()

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
        ): DoubleRatchet
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