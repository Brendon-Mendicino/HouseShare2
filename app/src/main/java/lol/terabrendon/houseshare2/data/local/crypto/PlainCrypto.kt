package lol.terabrendon.houseshare2.data.local.crypto

/**
 * No encryption at all, so the stored data can be inspected: debug builds only.
 */
object PlainCrypto : Crypto {
    override fun encrypt(bytes: ByteArray): ByteArray = bytes

    override fun decrypt(bytes: ByteArray): ByteArray = bytes
}
