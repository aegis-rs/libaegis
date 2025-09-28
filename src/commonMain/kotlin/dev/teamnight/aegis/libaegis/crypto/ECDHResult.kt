package dev.teamnight.aegis.libaegis.crypto

import dev.teamnight.aegis.libaegis.crypto.key.PrivateKey
import dev.teamnight.aegis.libaegis.crypto.key.PublicKey

expect class ECDHResult(privateKey: PrivateKey, publicKey: PublicKey) {
    val arrayResult: ByteArray
}