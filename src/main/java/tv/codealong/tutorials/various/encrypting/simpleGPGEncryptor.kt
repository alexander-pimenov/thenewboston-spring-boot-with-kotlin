package tv.codealong.tutorials.various.encrypting

class SimpleGPGEncryptor {
    fun encryptForCloud(filePath: String, password: String): String {
        val outputFile = "$filePath.gpg"
        val process = Runtime.getRuntime().exec(arrayOf(
            "gpg", "--symmetric", "--cipher-algo", "AES256",
            "--batch", "--passphrase", password,
            "--output", outputFile, filePath
        ))

        if (process.waitFor() == 0) {
            return outputFile
        } else {
            throw Exception("Ошибка шифрования")
        }
    }

    fun decryptFromCloud(encryptedFile: String, password: String): String {
        val outputFile = encryptedFile.removeSuffix(".gpg")
        val process = Runtime.getRuntime().exec(arrayOf(
            "gpg", "--decrypt", "--batch", "--passphrase", password,
            "--output", outputFile, encryptedFile
        ))

        if (process.waitFor() == 0) {
            return outputFile
        } else {
            throw Exception("Ошибка дешифрования: неверный пароль?")
        }
    }
}

// Использование:
fun main() {
    val encryptor = SimpleGPGEncryptor()

    // Файл в корне проекта
    encryptor.encryptForCloud("secrets.txt", "пароль")

    // Или явно указать путь из корня
    encryptor.encryptForCloud("./secrets.txt", "пароль")

    //В специальной папке config/
    //project/
    //├── app.jar
    //├── config/
    //│   ├── secrets.txt         # 👈 Лучше положить сюда
    //│   └── settings.properties
    //└── logs/
    encryptor.encryptForCloud("config/secrets.txt", "пароль")

    // Перед загрузкой в облако:
    val encrypted = encryptor.encryptForCloud("secrets.txt", "мой_надёжный_пароль")

    // После скачивания из облака:
    val decrypted = encryptor.decryptFromCloud("secrets.txt.gpg", "мой_надёжный_пароль")
}