package dev.teamnight.aegis.libaegis.crypto.key

import java.security.Key as JvmKey
import java.security.PrivateKey as JvmPrivateKey
import java.security.PublicKey as JvmPublicKey

actual interface Key {
    val jvmKey: JvmKey
}

actual interface PublicKey : Key {
    val jvmPublicKey: JvmPublicKey

    override val jvmKey
        get() = jvmPublicKey

    actual val raw: ByteArray
    actual val encoded: ByteArray

    actual companion object {
        actual fun fromRaw(raw: ByteArray): PublicKey {
            return publicKeyFromRaw(raw)
        }
    }
}

actual interface PrivateKey : Key {
    val jvmPrivateKey: JvmPrivateKey

    override val jvmKey
        get() = jvmPrivateKey

    actual fun destroy()
}

class JavaPublicKey(
    override val jvmPublicKey: JvmPublicKey,
) : PublicKey {
    override val raw: ByteArray
        get() = jvmPublicKey.raw

    override val encoded: ByteArray
        get() = jvmPublicKey.encoded
}

class JavaPrivateKey(
    override val jvmPrivateKey: JvmPrivateKey,
) : PrivateKey {
    override fun destroy() {
        jvmPrivateKey.destroy()
    }
}