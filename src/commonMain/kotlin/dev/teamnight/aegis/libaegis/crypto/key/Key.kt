package dev.teamnight.aegis.libaegis.crypto.key

expect interface Key

expect interface PublicKey : Key {
    val raw: ByteArray
    val encoded: ByteArray

    companion object {
        fun fromRaw(raw: ByteArray): PublicKey
    }
}

expect interface PrivateKey : Key {
    fun destroy()
}

data class KeyPair(val publicKey: PublicKey, val privateKey: PrivateKey)