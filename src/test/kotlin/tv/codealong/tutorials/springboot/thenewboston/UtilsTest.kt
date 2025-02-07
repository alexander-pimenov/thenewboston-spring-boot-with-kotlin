package tv.codealong.tutorials.springboot.thenewboston

import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.net.SocketException
import java.net.InetAddress

internal class UtilsTest {

    @Test
    fun `should call its data source to retrieve banks`() {

        //Определение операционной системы
        //Возвращает "linux", "mac os x" (для macOS), "windows" и т. д.
        //Можно использовать contains("mac") для проверки macOS.
        val osName = System.getProperty("os.name").lowercase()
        println("Operating System: $osName")

        //Получение имени пользователя
        //Возвращает имя текущего пользователя в системе.
        val userName = System.getProperty("user.name")
        println("User Name: $userName")

        //Определение IP-адреса
        //Получает IP-адрес локального хоста.
        //На macOS может вернуть 127.0.0.1, поэтому лучше использовать команду ifconfig или ipconfig для более точного IP.
        val ipAddress = InetAddress.getLocalHost().hostAddress
        println("IP Address =>>> $ipAddress")

        println("IP Address: ${getIpAddress2()}")


        printIpAddress()
        println("----------------------")
        getIpAddress()
    }
}

fun printIpAddress() {
    val command = if (System.getProperty("os.name").toLowerCase().contains("windows")) "ipconfig" else "ifconfig"
    println(command)
    val process = Runtime.getRuntime().exec(command)
    println(process)

    try {
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            var line: String? = null
            while (reader.readLine()?.also { line = it } != null) {
                if (line!!.contains("inet addr:")) {
                    val index = line!!.indexOf("inet addr:")
                    val ipAddress = line!!.substring(index + "inet addr:".length, line!!.length).trim()
                    println("IP address: $ipAddress")
                } else if (line!!.contains("inet ")) {
                    val index = line!!.indexOf("inet ")
                    val ipAddress = line!!.substring(index + "inet ".length, line!!.lastIndexOf(' ')).trim()
                    println("IP address: $ipAddress")
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun getIpAddress() {
    try {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()
            val inetAddresses = networkInterface.inetAddresses
            while (inetAddresses.hasMoreElements()) {
                val inetAddress = inetAddresses.nextElement()
                if (!inetAddress.isLoopbackAddress && !inetAddress.isLinkLocalAddress) {
                    println("IP address>>>: ${inetAddress.hostAddress}")
                }
            }
        }
    } catch (e: SocketException) {
        e.printStackTrace()
    }
}

//Можно использовать команду hostname -I (Linux) или ipconfig getifaddr en0 (macOS):
//Для macOS команда ipconfig getifaddr en0 вернёт реальный IP-адрес Wi-Fi.
//Для Linux hostname -I вернёт список IP-адресов (первый обычно локальный).
fun getIpAddress2(): String {
    val command = if (System.getProperty("os.name").lowercase().contains("mac")) {
        "ipconfig getifaddr en0"
    } else {
        "hostname -I"
    }

    return try {
        val process = Runtime.getRuntime().exec(command)
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        reader.readLine()?.trim() ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}


