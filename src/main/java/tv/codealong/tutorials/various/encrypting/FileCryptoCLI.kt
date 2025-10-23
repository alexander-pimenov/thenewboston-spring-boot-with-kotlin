package tv.codealong.tutorials.various.encrypting

import java.io.File
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class FileCryptoCLI {
    companion object {
        private const val ALGORITHM = "AES/CBC/PKCS5Padding"
        private const val KEY_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 65536
        private const val KEY_LENGTH = 256

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 3) {
                printHelp()
                return
            }

            val command = args[0]
            val inputFile = args[1]
            val outputFile = args[2]
            val password = if (args.size >= 4) args[3] else askForPassword()

            try {
                when (command) {
                    "encrypt" -> encryptFile(inputFile, outputFile, password)
                    "decrypt" -> decryptFile(inputFile, outputFile, password)
                    else -> {
                        println("Неизвестная команда: $command")
                        printHelp()
                    }
                }
                println("Успешно! Команда: $command")
                println("Входной файл: $inputFile")
                println("Выходной файл: $outputFile")
            } catch (e: Exception) {
                println("Ошибка: ${e.message}")
            }
        }

        private fun encryptFile(inputPath: String, outputPath: String, password: String) {
            val inputFile = File(inputPath)
            if (!inputFile.exists()) {
                throw IllegalArgumentException("Файл не найден: $inputPath")
            }

            val salt = ByteArray(16).apply { java.security.SecureRandom().nextBytes(this) }
            val iv = ByteArray(16).apply { java.security.SecureRandom().nextBytes(this) }

            val factory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
            val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            val secretKey: SecretKey = factory.generateSecret(spec)
            val secretKeySpec = SecretKeySpec(secretKey.encoded, "AES")

            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(iv))

            val inputBytes = inputFile.readBytes()
            val encryptedBytes = cipher.doFinal(inputBytes)

            // Сохраняем соль, IV и зашифрованные данные
            File(outputPath).outputStream().use { output ->
                output.write(salt)
                output.write(iv)
                output.write(encryptedBytes)
            }
        }

        private fun decryptFile(inputPath: String, outputPath: String, password: String) {
            val inputFile = File(inputPath)
            if (!inputFile.exists()) {
                throw IllegalArgumentException("Файл не найден: $inputPath")
            }

            val fileBytes = inputFile.readBytes()
            if (fileBytes.size < 32) {
                throw IllegalArgumentException("Файл слишком мал для расшифрования")
            }

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
            File(outputPath).writeBytes(decryptedBytes)
        }

        private fun askForPassword(): String {
            print("Введите пароль: ")
            return readLine() ?: throw IllegalArgumentException("Пароль обязателен")
        }

        private fun printHelp() {
            println("""
            Использование:
              java -jar crypto.jar encrypt <входной_файл> <выходной_файл> [пароль]
              java -jar crypto.jar decrypt <входной_файл> <выходной_файл> [пароль]
            
            Примеры:
              Шифрование:
                java -jar crypto.jar encrypt secrets.txt secrets.enc mypassword
                или
                java -jar crypto.jar encrypt "Интересные ссылки.txt" links.enc
                
              Дешифрование:
                java -jar crypto.jar decrypt secrets.enc secrets_decrypted.txt mypassword
                или
                java -jar crypto.jar decrypt links.enc "Расшифрованные ссылки.txt"
            
            Если пароль не указан, будет запрошен при запуске.
            """.trimIndent())
        }
    }
}