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
//        val ipAddress = InetAddress.getLocalHost().hostAddress
//        println("IP Address =>>> $ipAddress")

        //println("IP Address: ${getIpAddress2()}")
        println("IP Address: ${getIpAddressWithHelpProcessBuilder2()}")


//        printIpAddress()
        println("----------------------")
//        getIpAddress()
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
    } else if (System.getProperty("os.name").lowercase().contains("windows")) {
        "ipconfig"
    } else {
        "hostname -I"
    }

    return try {
        val process = Runtime.getRuntime().exec(command)
        println(process)
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        reader.readLine()?.trim() ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}

fun getIpAddressWithHelpProcessBuilder(): String {
    val command = if (System.getProperty("os.name").lowercase().contains("mac")) {
        listOf("ipconfig", "getifaddr", "en0") // macOS
    } else {
        listOf("hostname", "-I") // Linux
    }

    return try {
        val process = ProcessBuilder(command)
            .redirectErrorStream(true) // Перенаправляем stderr в stdout
            .start()
        println(process)
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val result = reader.readLine()?.trim() ?: "Unknown"
        process.waitFor() // Дождаться завершения процесса
        result
    } catch (e: Exception) {
        "Unknown"
    }
}

/**
 * Как это работает:
 * Определяем операционную систему (os.name).
 * Запускаем нужную команду:
 * Windows → ipconfig
 * macOS → ipconfig getifaddr en0
 * Linux → hostname -I
 * Считываем вывод команды с правильной кодировкой (UTF-8).
 * Для Windows извлекаем строку с "IPv4", парсим IP.
 *
 * 1. В Windows/macOS – использовать host.docker.internal (рекомендуется)
 * Для Windows и macOS Docker автоматически добавляет host.docker.internal, который ссылается на хостовую машину.
 *
 *  2. В Linux – получить IP docker0 (виртуальный интерфейс Docker)
 * В Linux хостовая машина доступна через специальный сетевой интерфейс docker0.
 * Чтобы узнать его IP, можно выполнить:
 *
 * sh
 * Копировать
 * Редактировать
 * ip -4 addr show docker0 | grep -oP '(?<=inet\s)\d+(\.\d+){3}'
 * Обычно это что-то вроде 172.17.0.1 или 192.168.xxx.1.
 *
 *
 */
fun getIpAddressWithHelpProcessBuilder2(): String {
    val osName = System.getProperty("os.name").lowercase()

    val command = when {
        osName.contains("win") -> listOf("cmd.exe", "/c", "ipconfig") // Windows
        osName.contains("mac") -> listOf("ipconfig", "getifaddr", "en0") // macOS
        else -> listOf("hostname", "-I") // Linux
    }

    return try {
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        val reader = BufferedReader(InputStreamReader(process.inputStream, Charsets.UTF_8))
        val output = reader.readText()
        process.waitFor()

        // Извлекаем IP-адрес из вывода
        when {
            osName.contains("win") -> extractWindowsIp(output)
            else -> output.trim().split(" ").firstOrNull() ?: "Unknown"
        }
    } catch (e: Exception) {
        "Unknown"
    }
}

fun extractWindowsIp(output: String): String {
    return output
        .lineSequence()
        .map { it.trim() }
        .firstOrNull { it.contains("IPv4", ignoreCase = true) }
        ?.apply { println(this) }
        ?.split(":")
        ?.getOrNull(1)
        ?.trim()
        ?: "Unknown"
}

fun getLinuxHostIp(): String {
    return try {
        val process = ProcessBuilder("sh", "-c", "ip -4 addr show docker0 | grep -oP '(?<=inet\\s)\\d+(\\.\\d+){3}'")
            .redirectErrorStream(true)
            .start()

        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val ip = reader.readLine()?.trim() ?: "localhost"
        process.waitFor()
        ip
    } catch (e: Exception) {
        "localhost"
    }
}

/**
 * В любой ОС – узнать IP хостовой машины через ip route
 * Альтернативный метод: можно выполнить команду
 *
 * sh
 * Копировать
 * Редактировать
 * ip route | grep default | awk '{print $3}'
 * Обычно это IP, через который контейнеры могут обращаться к хосту.
 */
fun getHostIp(): String {
    return try {
        val process = ProcessBuilder("sh", "-c", "ip route | grep default | awk '{print $3}'")
            .redirectErrorStream(true)
            .start()

        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val ip = reader.readLine()?.trim() ?: "localhost"
        process.waitFor()
        ip
    } catch (e: Exception) {
        "localhost"
    }
}

//Если тесты запускаются в контейнере, а сервис — на хосте, можно в коде автоматически подставлять нужный IP:
//
//kotlin
//Копировать
//Редактировать
//val hostIp = if (System.getProperty("os.name").lowercase().contains("win") ||
//                 System.getProperty("os.name").lowercase().contains("mac"))
//    "host.docker.internal"
//else
//    getLinuxHostIp()
//
//val serviceUrl = "http://$hostIp:8080"
//println("Using service URL: $serviceUrl")
//Теперь тесты всегда будут правильно обращаться к сервису, независимо от ОС и конфигурации Docker. 🚀



