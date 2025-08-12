package dev.teamnight.aegis.libaegis.key

import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.bouncycastle.crypto.util.PublicKeyFactory
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.NamedParameterSpec
import java.security.spec.XECPublicKeySpec

val PublicKey.raw: ByteArray
    get() {
        if(this.algorithm.equals("X25519")) {
            val params = PublicKeyFactory.createKey(this.encoded) as X25519PublicKeyParameters

            return params.encoded
        } else {
            throw UnsupportedOperationException("Unsupported key algorithm: ${this.algorithm}")
        }
    }

fun publicKeyFromRaw(bytes: ByteArray): PublicKey {
    val kf = KeyFactory.getInstance("X25519")
    val params = XECPublicKeySpec(NamedParameterSpec("X25519"), BigInteger(1, bytes))

    return kf.generatePublic(params)
}