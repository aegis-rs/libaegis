package dev.teamnight.aegis.libaegis.crypto

import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.KeyAgreement

class ECDHResult(privateKey: PrivateKey, publicKey: PublicKey) {
    val arrayResult: ByteArray

    init {
        val ka = KeyAgreement.getInstance("X25519")
        ka.init(privateKey)
        ka.doPhase(publicKey, true)

        this.arrayResult = ka.generateSecret()
    }
}