package dev.teamnight.aegis.libaegis.crypto.algorithm

import dev.teamnight.aegis.libaegis.crypto.key.KeyPair

expect class X25519 {
    companion object {
        fun generateKeyPair(): KeyPair
    }
}