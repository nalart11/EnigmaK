package org.makeacake.util

import org.makeacake.Gui
import org.makeacake.Main
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Class for initializing and using encryption
 */
object Encryption {
    private var key: SecretKey? = null
    private var iv: IvParameterSpec? = null
    private var encryptCipher: Cipher? = null
    private var decryptCipher: Cipher? = null

    /**
     * Getting the encryption key from the config
     * @throws EncryptionException Encryption key is empty
     * @throws IllegalArgumentException Key is invalid
     */
    @Throws(EncryptionException::class, IllegalArgumentException::class)
    fun initKey() {
        val keyString: List<String> = Main.config.getString("security-key").split("<->")

        if (keyString.size == 1) throw EncryptionException("Encryption key not specified")
        require(keyString.size == 2) { "Encryption key is incorrect" }

        key = SecretKeySpec(
            Base64.getDecoder().decode(keyString[0]),
            "AES"
        )
        iv = IvParameterSpec(
            Base64.getDecoder().decode(keyString[1])
        )
    }

    /**
     * Initializing encryption and decryption methods
     * @throws InvalidKeyException Invalid key
     */
    @Throws(Exception::class)
    fun initEncryption() {
        try {
            encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        } catch (e: Exception) {
            Gui.breakInput()
            Gui.showNewMessage(
                "An unexpected error occurred while initializing encryption (%s)".format(e.message),
                Gui.MessageType.SYSTEM_ERROR
            )
        }

        encryptCipher!!.init(Cipher.ENCRYPT_MODE, key, iv)
        decryptCipher!!.init(Cipher.DECRYPT_MODE, key, iv)
    }

    /**
     * Generate new valid key and encode id to String(base64)
     * @return Encoded key
     */
    fun generateNewKey(): String {
        try {
            val keygen = KeyGenerator.getInstance("AES")
            keygen.init(256)

            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)

            val encKey = Base64.getEncoder().encodeToString(keygen.generateKey().getEncoded())
            val ivString = Base64.getEncoder().encodeToString(iv)

            return "%s<->%s".format(encKey, ivString)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Generate new key and write it to file
     * @throws IOException Errors when writing key to file
     */
    @Throws(IOException::class)
    fun generateNewKeyFile() {
        Files.writeString(Paths.get("newkey.txt"), "New encryption key: %s".format(generateNewKey()))
    }

    /**
     * Encrypt text
     * @param text Text
     * @return Encrypted text (base64)
     * @throws Exception Wrong encryption key
     */
    @Throws(Exception::class)
    fun encrypt(text: String): String {
        val encryptedBytes = encryptCipher!!.doFinal(text.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    /**
     * Decrypt text
     * @param text Encrypted text (base64)
     * @return Decrypted text
     * @throws Exception Wrong encryption key
     */
    @Throws(Exception::class)
    fun decrypt(text: String?): String {
        val encryptedBytes = Base64.getDecoder().decode(text)
        val decryptedBytes = decryptCipher!!.doFinal(encryptedBytes)

        return String(decryptedBytes)
    }

    /**
     * Tells you that the encryption key is incorrect and closes the program
     */
    fun showIncorrectKeyError() {
        Gui.showNewMessage("Your encryption key is damaged or incorrect!", Gui.MessageType.SYSTEM_ERROR)
        Gui.showNewMessage(
            "Run the program with an empty encryption key to generate a new one",
            Gui.MessageType.SYSTEM_INFO
        )
        Gui.breakInput()
    }

    fun showNullKeyErrorAndGenerateNewOne() {
        Gui.showNewMessage("You haven't set the encryption key!", Gui.MessageType.SYSTEM_ERROR)
        try {
            generateNewKeyFile()
            Gui.showNewMessage("The new key is saved to the file new_key.txt", Gui.MessageType.SYSTEM_INFO)
        } catch (ex: IOException) {
            Gui.showNewMessage("An error occurred while generating and saving a new key", Gui.MessageType.SYSTEM_ERROR)
        }
        Gui.breakInput()
    }

    /**
     * Various encryption related errors.
     * The essence of the errors is conveyed in the message
     */
    class EncryptionException(message: String?) : Exception(message)
}