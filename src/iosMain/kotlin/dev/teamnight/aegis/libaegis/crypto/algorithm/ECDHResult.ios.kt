package dev.teamnight.aegis.libaegis.crypto.algorithm

import dev.teamnight.aegis.libaegis.crypto.key.PrivateKey
import dev.teamnight.aegis.libaegis.crypto.key.PublicKey

actual class ECDHResult actual constructor(
    privateKey: PrivateKey,
    publicKey: PublicKey
) {
    actual val arrayResult: ByteArray
        get() = TODO("Not yet implemented")
}