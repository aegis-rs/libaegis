package dev.teamnight.aegis.libaegis.crypto.key

import javax.security.auth.Destroyable
import java.security.Key as JvmKey
import java.security.PrivateKey as JvmPrivateKey
import java.security.PublicKey as JvmPublicKey

actual interface Key : Destroyable {
    fun toJavaKey(): JvmKey
}

actual interface PublicKey : Key {
    fun toJavaPublicKey(): JvmPublicKey

    override fun toJavaKey(): JvmKey = toJavaPublicKey() as JvmKey
}

actual interface PrivateKey : Key {
    fun toJavaPrivateKey(): JvmPrivateKey

    override fun toJavaKey(): JvmKey = toJavaPrivateKey() as JvmKey
}

class JavaPublicKey(
    val publicKey: JvmPublicKey,
) : PublicKey {
    override fun toJavaPublicKey(): JvmPublicKey = publicKey
}

class JavaPrivateKey(
    val privateKey: JvmPrivateKey,
) : PrivateKey {
    override fun toJavaPrivateKey(): JvmPrivateKey = privateKey

    override fun destroy() {
        privateKey.destroy()
    }
}