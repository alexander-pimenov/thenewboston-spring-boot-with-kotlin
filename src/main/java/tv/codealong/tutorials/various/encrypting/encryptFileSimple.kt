package tv.codealong.tutorials.various.encrypting

import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

class FileEncryptor {
    companion object {
        private const val ALGORITHM = "AES/CBC/PKCS5Padding"
        private const val KEY_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 65536
        private const val KEY_LENGTH = 256
    }

    fun encryptFile(inputFile: String, outputFile: String, password: String) {
        val salt = ByteArray(16).apply { java.security.SecureRandom().nextBytes(this) }
        val iv = ByteArray(16).apply { java.security.SecureRandom().nextBytes(this) }

        val factory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val secretKey: SecretKey = factory.generateSecret(spec)
        val secretKeySpec = SecretKeySpec(secretKey.encoded, "AES")

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(iv))

        val inputBytes = java.io.File(inputFile).readBytes()
        val encryptedBytes = cipher.doFinal(inputBytes)

        // Сохраняем соль, IV и зашифрованные данные
        java.io.File(outputFile).outputStream().use { output ->
            output.write(salt)
            output.write(iv)
            output.write(encryptedBytes)
        }
    }

    fun decryptFile(inputFile: String, outputFile: String, password: String) {
        val fileBytes = java.io.File(inputFile).readBytes()

        val salt = fileBytes.copyOfRange(0, 16)
        val iv = fileBytes.copyOfRange(16, 32)
        val encryptedData = fileBytes.copyOfRange(32, fileBytes.size)

        val factory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val secretKey: SecretKey = factory.generateSecret(spec)
        val secretKeySpec = SecretKeySpec(secretKey.encoded, "AES")

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, IvParameterSpec(iv))

        val decryptedBytes = cipher.doFinal(encryptedData)
        java.io.File(outputFile).writeBytes(decryptedBytes)
    }
}

// Использование:
fun main() {
    val encryptor = FileEncryptor()

    // Шифрование
    encryptor.encryptFile("secrets.txt", "secrets.enc", "мой_сложный_пароль")

    // Дешифрование
    encryptor.decryptFile("secrets.enc", "secrets_decrypted.txt", "мой_сложный_пароль")
}