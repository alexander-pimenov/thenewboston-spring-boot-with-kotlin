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
import java.time.LocalDateTime

// ✅ Исправленный sealed class
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
        """.trimIndent()
    }

    fun isHidden(): Boolean = name.startsWith(".")

    fun lastModifiedDaysAgo(): Long {
        return java.time.Duration.between(modified, LocalDateTime.now()).toDays()
    }

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
    val fileName: String = "file" // отдельное поле для имени файла
) : FileSystemNode(
    name = if (extension.isNotEmpty()) "$fileName.$extension" else fileName, // передаём в родительский конструктор
    size = content.toByteArray().size.toLong()
) {
    override val path: String = "$parentPath/$name"

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
            "kt", "java" -> "Source Code"
            else -> "Unknown File Type"
        }
    }
}

// 📁 ДИРЕКТОРИЯ
data class Directory(
    val children: List<FileSystemNode> = emptyList(),
    val parentPath: String = "/",
    override val permissions: String = "rwxr-xr-x",
    val dirName: String = "directory" // отдельное поле для имени директории
) : FileSystemNode(
    name = dirName, // передаём в родительский конструктор
    size = children.sumOf { it.size }
) {
    override val path: String = "$parentPath/$name"

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
    val linkName: String = "link" // отдельное поле для имени ссылки
) : FileSystemNode(
    name = "link_to_${target.name}", // передаём в родительский конструктор
    size = 64 // symbolic links have small fixed size
) {
    override val path: String = "$parentPath/$name"

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

// 🎯 ИСПРАВЛЕННЫЙ ПРИМЕР ИСПОЛЬЗОВАНИЯ
fun main() {
    println("=== 🗂️ ФАЙЛОВАЯ СИСТЕМА ===")

    // Создаём файлы (теперь с правильными параметрами)
    val readme = File(
        content = """
            # My Project
            This is a sample project
            with multiple lines of text
        """.trimIndent(),
        extension = "md",
        fileName = "README" // задаём имя файла
    )

    val config = File(
        content = """
            app.name=MyApp
            app.version=1.0.0
            database.url=localhost:5432
        """.trimIndent(),
        extension = "properties",
        fileName = "config"
    )

    val image = File(
        content = "fake image content",
        extension = "jpg",
        fileName = "photo"
    )

    // Создаём поддиректорию (теперь с правильными параметрами)
    val srcDirectory = Directory(
        children = listOf(
            File(
                content = "fun main() {}",
                extension = "kt",
                fileName = "Main"
            ),
            File(
                content = "class User {}",
                extension = "kt",
                fileName = "User"
            )
        ),
        dirName = "src" // задаём имя директории
    )

    // Создаём корневую директорию (теперь с правильными параметрами)
    val rootDirectory = Directory(
        children = listOf(readme, config, image, srcDirectory),
        dirName = "projects" // задаём имя директории
    )

    // Создаём символическую ссылку (теперь с правильными параметрами)
    val readmeLink = SymLink(
        target = readme,
        linkName = "quick_start"
    )

    // 🔍 АНАЛИЗИРУЕМ ФАЙЛОВУЮ СИСТЕМУ
    analyzeFileSystem(rootDirectory)

    println("\n=== 🔎 ПОИСК И ФИЛЬТРАЦИЯ ===")
    searchInFileSystem(rootDirectory)

    println("\n=== 🛠️ ОПЕРАЦИИ С ФАЙЛАМИ ===")
    fileOperationsDemo(readme)

    println("\n=== 🔗 ТЕСТИРУЕМ ССЫЛКИ ===")
    testSymLink(readmeLink)
}

fun analyzeFileSystem(directory: Directory) {
    println("📊 АНАЛИЗ ДИРЕКТОРИИ: ${directory.name}")
    println("=" * 50)

    println("📁 Общая информация:")
    println(directory.getInfo())

    println("\n📋 Содержимое:")
    directory.children.forEach { node ->
        when (node) {
            is File -> {
                println("📄 ${node.name} (${node.getFileType()}) - ${node.size}")
                if (node.extension == "md" || node.extension == "kt") {
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

    if (updatedFile.getLineCount() > 3) {
        println("... и ещё ${updatedFile.getLineCount() - 3} строк")
    }
}

fun testSymLink(symLink: SymLink) {
    println("🔗 Тестируем символическую ссылку: ${symLink.name}")
    println("🎯 Цель: ${symLink.resolveTarget().name}")
    println("❌ Ссылка битая: ${symLink.isBroken()}")
    println("📊 Размер ссылки: ${symLink.size} bytes")
}

// Вспомогательная функция для повторения строк
operator fun String.times(n: Int): String = repeat(n)