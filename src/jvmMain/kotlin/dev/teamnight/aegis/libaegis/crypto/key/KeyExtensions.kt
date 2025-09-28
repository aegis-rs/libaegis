package dev.teamnight.aegis.libaegis.crypto.key

import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.bouncycastle.crypto.util.PublicKeyFactory
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.KeyFactory
import java.security.PublicKey
import java.security.interfaces.XECPublicKey
import java.security.spec.NamedParameterSpec
import java.security.spec.X509EncodedKeySpec

val PublicKey.raw: ByteArray
    get() {
        if (!this.algorithm.equals("XDH") && this !is XECPublicKey && this.params !is NamedParameterSpec) {
            throw UnsupportedOperationException("Unsupported key algorithm: ${this.algorithm}")
        }

        val spec = this.params as NamedParameterSpec

        if (spec.name.equals("X25519")) {
            val params = PublicKeyFactory.createKey(this.encoded) as X25519PublicKeyParameters

            return params.encoded
        } else {
            throw UnsupportedOperationException("Unsupported key algorithm: ${this.algorithm}")
        }
    }

fun publicKeyFromRaw(bytes: ByteArray): JavaPublicKey {
    val params = X25519PublicKeyParameters(bytes, 0)

    val spki = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(params)

    val kf = KeyFactory.getInstance("XDH", BouncyCastleProvider())
    val spec = X509EncodedKeySpec(spki.encoded)

    return JavaPublicKey(kf.generatePublic(spec))
}