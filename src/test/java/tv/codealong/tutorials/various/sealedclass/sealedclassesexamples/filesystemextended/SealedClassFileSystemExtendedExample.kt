package tv.codealong.tutorials.various.sealedclass.sealedclassesexamples.filesystemextended

import java.time.LocalDateTime

/**
 * sealed class - Это как иметь общий интерфейс + общую реализацию + типобезопасность! 🚀
 * File System:
 * - Общее состояние: name, created, modified, size
 * - Общие методы: getInfo(), isHidden(), lastModifiedDaysAgo()
 * - Специфичные методы: у каждого типа свои уникальные операции
 */
//5. File System Example
//
// ✅ Файловая система
// ✅ Улучшенная файловая система с реальными методами
sealed class FileSystemNode(
    val name: String,
    val created: LocalDateTime = LocalDateTime.now(),
    val modified: LocalDateTime = LocalDateTime.now(),
    open val size: Long
) {
    abstract val permissions: String
    abstract val path: String

    // ОБЩИЕ МЕТОДЫ для всех узлов файловой системы
    fun getInfo(): String {
        return """
            📁 Имя: $name
            📍 Путь: $path
            📏 Размер: ${size.formatSize()}
            🔐 Права: $permissions
            🕐 Создан: $created
            ✏️ Изменён: $modified
            👤 Владелец: ${getOwner()}
        """.trimIndent()
    }

    fun isHidden(): Boolean = name.startsWith(".")

    fun lastModifiedDaysAgo(): Long {
        return java.time.Duration.between(modified, LocalDateTime.now()).toDays()
    }

    abstract fun getOwner(): String
    abstract fun copy(newPath: String): FileSystemNode

    // Форматирование размера файла
    fun Long.formatSize(): String {
        return when {
            this < 1024 -> "$this bytes"
            this < 1024 * 1024 -> "%.1f KB".format(this / 1024.0)
            else -> "%.1f MB".format(this / (1024.0 * 1024.0))
        }
    }
}

// 📄 ФАЙЛ
data class File(
    val content: String,
    val extension: String,
    val parentPath: String = "/",
    override val permissions: String = "rw-r--r--",
    val owner: String = "user"
) : FileSystemNode(
    name = "file.${if (extension.isNotEmpty()) ".$extension" else ""}",
    size = content.toByteArray().size.toLong()
) {
    override val path: String = "$parentPath/$name"

    override fun getOwner(): String = owner

    override fun copy(newPath: String): FileSystemNode {
        return this.copy(parentPath = newPath)
    }

    // ФАЙЛ-СПЕЦИФИЧНЫЕ МЕТОДЫ
    fun readLines(): List<String> = content.lines()

    fun appendText(newText: String): File {
        return this.copy(content = content + newText)
    }

    fun search(keyword: String): Boolean {
        return content.contains(keyword, ignoreCase = true)
    }

    fun getLineCount(): Int = content.lines().size

    fun getFileType(): String {
        return when (extension.lowercase()) {
            "txt", "md" -> "Text Document"
            "jpg", "png", "gif" -> "Image File"
            "mp4", "avi" -> "Video File"
            "mp3", "wav" -> "Audio File"
            else -> "Unknown File Type"
        }
    }
}

// 📁 ДИРЕКТОРИЯ
data class Directory(
    val children: List<FileSystemNode> = emptyList(),
    val parentPath: String = "/",
    override val permissions: String = "rwxr-xr-x",
    val owner: String = "user"
) : FileSystemNode(
    name = "directory",
    size = children.sumOf { it.size }
) {
    override val path: String = "$parentPath/$name"

    override fun getOwner(): String = owner

    override fun copy(newPath: String): FileSystemNode {
        return this.copy(parentPath = newPath)
    }

    // ДИРЕКТОРИЯ-СПЕЦИФИЧНЫЕ МЕТОДЫ
    fun findFile(filename: String): File? {
        return children.filterIsInstance<File>().find { it.name == filename }
    }

    fun getAllFiles(): List<File> {
        return children.filterIsInstance<File>()
    }

    fun getAllDirectories(): List<Directory> {
        return children.filterIsInstance<Directory>()
    }

    fun addNode(node: FileSystemNode): Directory {
        return this.copy(children = children + node)
    }

    fun removeNode(nodeName: String): Directory {
        return this.copy(children = children.filter { it.name != nodeName })
    }

    fun getTotalFileCount(): Int = getAllFiles().size

    fun findFilesByExtension(ext: String): List<File> {
        return getAllFiles().filter { it.extension == ext }
    }

    fun calculateTotalSize(): String {
        val totalBytes = children.sumOf { it.size }
        return totalBytes.formatSize()
    }
}

// 🔗 СИМВОЛИЧЕСКАЯ ССЫЛКА
data class SymLink(
    val target: FileSystemNode,
    val parentPath: String = "/",
    override val permissions: String = "rwxrwxrwx",
    val owner: String = "user"
) : FileSystemNode(
    name = "link_to_${target.name}",
    size = 64 // symbolic links have small fixed size
) {
    override val path: String = "$parentPath/$name"

    override fun getOwner(): String = owner

    override fun copy(newPath: String): FileSystemNode {
        return this.copy(parentPath = newPath)
    }

    // ССЫЛКА-СПЕЦИФИЧНЫЕ МЕТОДЫ
    fun resolveTarget(): FileSystemNode = target

    fun isBroken(): Boolean {
        // В реальной системе здесь была бы проверка существования target
        return false
    }
}

// 🎯 ПРИМЕР ИСПОЛЬЗОВАНИЯ
fun main() {
    println("=== 🗂️ ФАЙЛОВАЯ СИСТЕМА ===")

    // Создаём файлы
    val readme = File(
        content = """
            # My Project
            This is a sample project
            with multiple lines of text
        """.trimIndent(),
        extension = "md",
        owner = "developer"
    )

    val config = File(
        content = """
            app.name=MyApp
            app.version=1.0.0
            database.url=localhost:5432
        """.trimIndent(),
        extension = "properties",
        owner = "admin"
    )

    val image = File(
        content = "fake image content", // в реальности это были бы байты
        extension = "jpg",
        owner = "user"
    )

    // Создаём поддиректорию
    val srcDirectory = Directory(
        children = listOf(
            File(content = "fun main() {}", extension = "kt", owner = "developer"),
            File(content = "class User {}", extension = "kt", owner = "developer")
        ),
        name = "src",
        owner = "developer"
    )

    // Создаём корневую директорию
    val rootDirectory = Directory(
        children = listOf(readme, config, image, srcDirectory),
        name = "projects",
        owner = "root"
    )

    // Создаём символическую ссылку
    val readmeLink = SymLink(target = readme, name = "quick_start")

    // 🔍 АНАЛИЗИРУЕМ ФАЙЛОВУЮ СИСТЕМУ
    analyzeFileSystem(rootDirectory)

    println("\n=== 🔎 ПОИСК И ФИЛЬТРАЦИЯ ===")
    searchInFileSystem(rootDirectory)

    println("\n=== 🛠️ ОПЕРАЦИИ С ФАЙЛАМИ ===")
    fileOperationsDemo(readme)
}

fun analyzeFileSystem(directory: Directory) {
    println("📊 АНАЛИЗ ДИРЕКТОРИИ: ${directory.name}")
    println("==================================================")

    println("📁 Общая информация:")
    println(directory.getInfo())

    println("\n📋 Содержимое:")
    directory.children.forEach { node ->
        when (node) {
            is File -> {
                println("📄 ${node.name} (${node.getFileType()}) - ${node.size}")
                if (node.extension == "md") {
                    println("   📝 Строк: ${node.getLineCount()}")
                }
            }
            is Directory -> {
                println("📁 ${node.name}/ - файлов: ${node.getTotalFileCount()}")
            }
            is SymLink -> {
                println("🔗 ${node.name} -> ${node.target.name}")
            }
        }
    }

    println("\n📈 Статистика:")
    println("• Всего файлов: ${directory.getTotalFileCount()}")
    println("• Всего директорий: ${directory.getAllDirectories().size}")
    println("• Kotlin файлов: ${directory.findFilesByExtension("kt").size}")
    println("• Общий размер: ${directory.calculateTotalSize()}")
}

fun searchInFileSystem(directory: Directory) {
    val keyword = "project"

    println("🔍 Поиск файлов содержащих '$keyword':")
    directory.getAllFiles().forEach { file ->
        if (file.search(keyword)) {
            println("✅ Найдено в: ${file.name}")
            println("   Тип: ${file.getFileType()}")
        }
    }

    println("\n📅 Файлы изменённые более 0 дней назад:")
    directory.getAllFiles().forEach { file ->
        val daysAgo = file.lastModifiedDaysAgo()
        if (daysAgo == 0L) {
            println("🕐 ${file.name} - изменён сегодня")
        }
    }
}

fun fileOperationsDemo(file: File) {
    println("🛠️ Демонстрация операций с файлом: ${file.name}")

    val updatedFile = file.appendText("\n\n## New Section\nAdded via program")
    println("✅ Файл обновлён. Новое количество строк: ${updatedFile.getLineCount()}")

    println("📖 Содержимое:")
    updatedFile.readLines().take(3).forEachIndexed { index, line ->
        println("   ${index + 1}: $line")
    }

    println("... и ещё ${updatedFile.getLineCount() - 3} строк")
}