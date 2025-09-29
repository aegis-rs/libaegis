package dev.teamnight.aegis.libaegis.crypto.algorithm

expect class HmacSha256 {
    companion object {
        fun digest(key: ByteArray, data: ByteArray): ByteArray
    }
}