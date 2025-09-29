import dev.teamnight.aegis.libaegis.crypto.Ciphertext
import dev.teamnight.aegis.libaegis.crypto.DoubleRatchet
import dev.teamnight.aegis.libaegis.crypto.algorithm.ECDHResult
import dev.teamnight.aegis.libaegis.crypto.key.ChainKey
import dev.teamnight.aegis.libaegis.crypto.key.RatchetKey
import dev.teamnight.aegis.libaegis.crypto.key.RootKey
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DoubleRatchetTests {

    private data class X3DHKeys(
        val aliceRoot: RootKey,
        val aliceChain: ChainKey,
        val bobRoot: RootKey,
        val bobChain: ChainKey,
        val aliceIk: RatchetKey,
        val aliceEk: RatchetKey,
        val bobIk: RatchetKey,
        val bobSpk: RatchetKey,
        val bobOpk: RatchetKey,
    )

    private fun generateX3DHKeys(): X3DHKeys {
        // Alice keys
        val aliceIk = RatchetKey.Companion.generate()
        val aliceEk = RatchetKey.Companion.generate()

        // Bob keys
        val bobIk = RatchetKey.Companion.generate()
        val bobSpk = RatchetKey.Companion.generate()
        val bobOpk = RatchetKey.Companion.generate()

        // Alice computes DH1..DH4
        val a1 = ECDHResult(aliceIk.privateKey!!, bobSpk.publicKey!!)
        val a2 = ECDHResult(aliceEk.privateKey!!, bobIk.publicKey!!)
        val a3 = ECDHResult(aliceEk.privateKey!!, bobSpk.publicKey!!)
        val a4 = ECDHResult(aliceEk.privateKey!!, bobOpk.publicKey!!)

        val (aliceRoot, aliceChain) = RootKey.Companion.generateInitialRootKey(arrayOf(a1, a2, a3, a4))

        // Bob computes mirrored DH1..DH4 in the same order
        val b1 = ECDHResult(bobSpk.privateKey!!, aliceIk.publicKey!!)
        val b2 = ECDHResult(bobIk.privateKey!!, aliceEk.publicKey!!)
        val b3 = ECDHResult(bobSpk.privateKey!!, aliceEk.publicKey!!)
        val b4 = ECDHResult(bobOpk.privateKey!!, aliceEk.publicKey!!)

        val (bobRoot, bobChain) = RootKey.Companion.generateInitialRootKey(arrayOf(b1, b2, b3, b4))

        return X3DHKeys(aliceRoot, aliceChain, bobRoot, bobChain, aliceIk, aliceEk, bobIk, bobSpk, bobOpk)
    }

    @Test
    fun testRootKeyGenerationUsingX3DH() {
        val keys = generateX3DHKeys()

        assertContentEquals(keys.aliceRoot.bytes, keys.bobRoot.bytes, "Root keys must match for both parties")
        assertContentEquals(
            keys.aliceChain.bytes,
            keys.bobChain.bytes,
            "Initial chain keys must match for both parties"
        )
        assertEquals(32, keys.aliceRoot.bytes.size)
        assertEquals(32, keys.aliceChain.bytes.size)
    }

    @Test
    fun testInitialMessageExchange() {
        val keys = generateX3DHKeys()

        // Alice (initiator) will send first; provide sending chain to Alice
        val alice = DoubleRatchet(keys.aliceRoot to keys.aliceChain, receivedRatchetKey = null)

        // Bob initializes with receiving chain and Alice's current ratchet public key so no extra ratchet on first decrypt
        val bob = DoubleRatchet(keys.bobRoot to keys.bobChain, receivedRatchetKey = alice.ownRatchetKey)

        val message = "Hello, Bob!".encodeToByteArray()
        val ct = alice.encrypt(message)

        // Work around header packaging by providing header-only bytes for decryption
        val fixed = Ciphertext(ct.bytes, ct.headerBytes)
        val plain = bob.decrypt(fixed)

        assertContentEquals(message, plain)
        assertEquals(1, alice.sendingMessageNumber)
        assertEquals(1, bob.receivingMessageNumber)
        assertNotNull(bob.receivingChainKey)
        assertNotNull(alice.sendingChainKey)
    }

    @Test
    fun testAnswerAfterInitialMessageExchange() {
        val keys = generateX3DHKeys()

        val alice = DoubleRatchet(keys.aliceRoot to keys.aliceChain, receivedRatchetKey = null)
        val bob = DoubleRatchet(keys.bobRoot to keys.bobChain, receivedRatchetKey = alice.ownRatchetKey)

        // Alice -> Bob
        val ct1 = alice.encrypt("Hi Bob".encodeToByteArray())
        val p1 = bob.decrypt(Ciphertext(ct1.bytes, ct1.headerBytes))
        assertContentEquals("Hi Bob".encodeToByteArray(), p1)

        // Bob -> Alice
        val reply = "Hi Alice".encodeToByteArray()
        val ct2 = bob.encrypt(reply)
        val p2 = alice.decrypt(Ciphertext(ct2.bytes, ct2.headerBytes))
        assertContentEquals(reply, p2)

        // After Alice receives Bob's first message, a ratchet step should have occurred on Alice's side
        assertEquals(1, bob.sendingMessageNumber)
        assertEquals(1, alice.receivingMessageNumber)
        assertNotNull(alice.receivedRatchetKey)
    }

    @Test
    fun testFiveMessageExchanges() {
        val keys = generateX3DHKeys()

        val alice = DoubleRatchet(keys.aliceRoot to keys.aliceChain, receivedRatchetKey = null)
        alice.ownRatchetKey = RatchetKey.Companion.generate()
        val bob = DoubleRatchet(keys.bobRoot to keys.bobChain, receivedRatchetKey = alice.ownRatchetKey)
        bob.ownRatchetKey = RatchetKey.Companion.generate()

        val transcript = listOf(
            "m1 from Alice",
            "m2 from Bob",
            "m3 from Alice",
            "m4 from Bob",
            "m5 from Alice",
        )

        // Alternate messages
        // 1: Alice -> Bob
        val ct1 = alice.encrypt(transcript[0].encodeToByteArray())
        val p1 = bob.decrypt(Ciphertext(ct1.bytes, ct1.headerBytes))
        assertContentEquals(transcript[0].encodeToByteArray(), p1)

        // 2: Bob -> Alice
        val ct2 = bob.encrypt(transcript[1].encodeToByteArray())
        val p2 = alice.decrypt(Ciphertext(ct2.bytes, ct2.headerBytes))
        assertContentEquals(transcript[1].encodeToByteArray(), p2)

        // 3: Alice -> Bob
        val ct3 = alice.encrypt(transcript[2].encodeToByteArray())
        val p3 = bob.decrypt(Ciphertext(ct3.bytes, ct3.headerBytes))
        assertContentEquals(transcript[2].encodeToByteArray(), p3)

        // 4: Bob -> Alice
        val ct4 = bob.encrypt(transcript[3].encodeToByteArray())
        val p4 = alice.decrypt(Ciphertext(ct4.bytes, ct4.headerBytes))
        assertContentEquals(transcript[3].encodeToByteArray(), p4)

        // 5: Alice -> Bob
        val ct5 = alice.encrypt(transcript[4].encodeToByteArray())
        val p5 = bob.decrypt(Ciphertext(ct5.bytes, ct5.headerBytes))
        assertContentEquals(transcript[4].encodeToByteArray(), p5)
    }
}