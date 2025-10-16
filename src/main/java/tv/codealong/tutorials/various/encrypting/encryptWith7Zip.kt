package tv.codealong.tutorials.various.encrypting

fun encryptWith7Zip(inputFile: String, outputFile: String, password: String) {
    val process = Runtime.getRuntime().exec(
        arrayOf("7z", "a", "-p$password", "-mhe=on", outputFile, inputFile)
    )
    process.waitFor()
}