package dev.teamnight.aegis.libaegis.crypto.algorithm

import dev.teamnight.aegis.libaegis.crypto.key.PrivateKey
import dev.teamnight.aegis.libaegis.crypto.key.PublicKey
import javax.crypto.KeyAgreement

actual class ECDHResult actual constructor(privateKey: PrivateKey, publicKey: PublicKey) {
    actual val arrayResult: ByteArray

    init {
        val ka = KeyAgreement.getInstance("X25519")
        ka.init(privateKey.jvmPrivateKey)
        ka.doPhase(publicKey.jvmPublicKey, true)

        this.arrayResult = ka.generateSecret()
    }
}