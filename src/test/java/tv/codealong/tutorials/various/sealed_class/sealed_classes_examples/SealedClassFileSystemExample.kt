package tv.codealong.tutorials.various.sealed_class.sealed_classes_examples

import java.time.LocalDateTime

//5. File System Example
//
// ✅ Файловая система
sealed class FileSystemNode(
    val name: String,
    val created: LocalDateTime,
    open val size: Long
) {
    abstract val permissions: String

    // Общие методы
    fun getPath(): String = name // упрощённо
    fun isOlderThan(days: Int): Boolean {
        return created.isBefore(LocalDateTime.now().minusDays(days.toLong()))
    }

    data class File(
        val content: ByteArray,
        val extension: String,
        override val permissions: String = "rw-r--r--"
    ) : FileSystemNode(
        name = "file.$extension",
        created = LocalDateTime.now(),
        size = content.size.toLong()
    ) {
        // File-specific methods
        fun readContent(): String = String(content)
    }

    data class Directory(
        val children: List<FileSystemNode> = emptyList(),
        override val permissions: String = "rwxr-xr-x"
    ) : FileSystemNode(
        name = "directory",
        created = LocalDateTime.now(),
        size = children.sumOf { it.size }
    ) {
        // Directory-specific methods
        fun findFile(filename: String): File? {
            return children.filterIsInstance<File>().find { it.name == filename }
        }
    }

    data class SymLink(
        val target: FileSystemNode,
        override val permissions: String = "rwxrwxrwx"
    ) : FileSystemNode(
        name = "link_to_${target.name}",
        created = LocalDateTime.now(),
        size = 0
    )
}

// Использование
fun analyzeFileSystem(node: FileSystemNode) {
    println("Анализ: ${node.name}")
    println("Размер: ${node.size} bytes")
    println("Права: ${node.permissions}")
    println("Создан: ${node.created}")

    when (node) {
        is FileSystemNode.File -> {
            println("Тип: Файл (.${node.extension})")
            println("Контент: ${node.readContent().take(50)}...")
        }
        is FileSystemNode.Directory -> {
            println("Тип: Директория")
            println("Количество файлов: ${node.children.size}")
        }
        is FileSystemNode.SymLink -> {
            println("Тип: Ссылка")
            println("Цель: ${node.target.name}")
        }
    }
}