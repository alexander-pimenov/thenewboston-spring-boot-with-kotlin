package tv.codealong.tutorials.various.encrypting

fun encryptWithGPGPassword(inputFile: String, outputFile: String, password: String) {
    val process = Runtime.getRuntime().exec(
        arrayOf("gpg", "--symmetric", "--cipher-algo", "AES256",
            "--batch", "--passphrase", password,
            "--output", outputFile, inputFile)
    )
    process.waitFor()
}

fun decryptWithGPGPassword(inputFile: String, outputFile: String, password: String) {
    val process = Runtime.getRuntime().exec(
        arrayOf("gpg", "--decrypt", "--batch", "--passphrase", password,
            "--output", outputFile, inputFile)
    )
    process.waitFor()
}

// Использование:
fun main() {
    // Шифруем с паролем
    encryptWithGPGPassword("secrets.txt", "secrets.gpg", "мой_пароль")

    // На другом компьютере:
    decryptWithGPGPassword("secrets.gpg", "secrets.txt", "мой_пароль")
}


fun decryptWithGPG(inputFile: String, outputFile: String) {
    val process = Runtime.getRuntime().exec(
        arrayOf("gpg", "--decrypt", "--output", outputFile, inputFile)
    )
    process.waitFor()
}

fun createGpgKey() {
    // GPG сам генерирует ключи правильно
    Runtime.getRuntime().exec(arrayOf(
        "gpg", "--full-generate-key",
        "--rsa", "4096",
        "--pinentry-mode", "loopback"
    ))
}

fun encryptForRecipient(inputFile: String, recipient: String) {
    // GPG сам выбирает лучшие параметры
    Runtime.getRuntime().exec(arrayOf(
        "gpg", "--encrypt",
        "--recipient", recipient,
        "--armor",  // ASCII output для читаемости
        "--output", "$inputFile.asc",
        inputFile
    ))
}

