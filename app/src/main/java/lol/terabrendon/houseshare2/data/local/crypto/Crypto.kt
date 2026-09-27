package lol.terabrendon.houseshare2.data.local.crypto

/**
 * Symmetric encryption of the data stored on the device, see [KeystoreCrypto] and [PlainCrypto].
 */
interface Crypto {
    fun encrypt(bytes: ByteArray): ByteArray

    fun decrypt(bytes: ByteArray): ByteArray
}
