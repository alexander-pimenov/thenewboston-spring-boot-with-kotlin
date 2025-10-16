package tv.codealong.tutorials.various.encrypting

import java.io.File
import java.io.FileNotFoundException

/**
 * Умное решение - искать файл в разных местах
 */
class SmartFileEncryptor {
    private fun findSecretsFile(): File {
        val possiblePaths = listOf(
            "secrets.txt",                   // Текущая директория
            "config/secrets.txt",            // Папка config
            "../secrets.txt",                // На уровень выше
            "src/main/resources/secrets.txt" // В ресурсах (для разработки)
        )

        for (path in possiblePaths) {
            val file = File(path)
            if (file.exists()) {
                println("Найден файл: ${file.absolutePath}")
                return file
            }
        }

        throw FileNotFoundException("Не найден secrets.txt")
    }

    fun encryptSecrets(password: String) {
        val secretsFile = findSecretsFile()
        val encryptor = SimpleGPGEncryptor()
        encryptor.encryptForCloud(secretsFile.path, password)
    }
}

// Использование:
fun main() {
    val smartEncryptor = SmartFileEncryptor()
    smartEncryptor.encryptSecrets("мой_пароль")
}