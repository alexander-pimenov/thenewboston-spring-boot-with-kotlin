package tv.codealong.tutorials.springboot.thenewboston.utils;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * Утилитный класс для безопасного вывода.
 * Есть проблема с корректным отображением символов кириллицы в консоли.
 * <p>
 * ## Архитектура SafePrinter
 * <p>
 * ### **1. Основная идея**
 * <p>
 * Проблема: `System.out` по умолчанию использует системную кодировку (в вашем случае Cp1252), которая не поддерживает кириллицу корректно.
 * <p>
 * Решение: Создать собственный `PrintStream` с явным указанием кодировки UTF-8 и использовать его вместо стандартного `System.out`.
 *
 * <p>
 * ### **3. Как это работает технически**
 * <p>
 * Когда вы делаете:
 * ```java
 * PrintStream utf8Out = new PrintStream(System.out, true, "UTF-8");
 * ```
 * Создается цепочка:
 * ```
 * Ваша строка (Java String, Unicode)
 *     → UTF-8 кодировка
 *     → Байтовый поток
 *     → Оригинальный System.out
 *     → Консоль IDEA
 * ```
 * ### **4. Почему это решает проблему?**
 * **Без SafePrinter:**
 * ```
 * "привет" (Unicode в Java)
 *     → System.out с Cp1252
 *     → "??????" (неправильная кодировка)
 * ```
 * **С SafePrinter:**
 * ```
 * "привет" (Unicode в Java)
 *     → SafePrinter с UTF-8
 *     → Байты в UTF-8
 *     → System.out
 *     → Консоль IDEA с UTF-8
 *     → "привет" ✓
 * ```
 * <p>
 * ## Почему SafePrinter — хорошее решение?
 * ```
 * 1. **Локализованная проблема** — влияет только на ваш код, не меняет глобальное состояние
 * 2. **Безопасность** — не ломает другие библиотеки
 * 3. **Явность** — явно видно, где используется безопасный вывод
 * 4. **Гибкость** — легко добавить дополнительные функции (логирование, форматирование)
 * 5. **Производительность** — статические методы, нет накладных расходов на создание объектов
 * ```
 * ## Ключевые уроки:
 * ```
 * 1. **Кодировка в Java — это преобразование Unicode → байты**
 * 2. **System.out по умолчанию использует системную кодировку**
 * 3. **PrintStream с явной кодировкой решает проблему**
 * 4. **Статические блоки инициализации выполняются при загрузке класса**
 * 5. **Лучше создавать свои утилиты, чем менять глобальное состояние**
 * ```
 * Теперь у нас есть мощный инструмент для работы с UTF-8 выводом в Java!
 */
public class SafePrinter {
    // Статические поля для потоков вывода
    private static final PrintStream UTF8_OUT;
    private static final PrintStream UTF8_ERR;
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    // Уровни логирования
    public enum Level {
        DEBUG, INFO, WARN, ERROR
    }

    // Текущий уровень логирования
    private static Level currentLevel = Level.INFO;

    // Статический блок инициализации - выполняется ПРИ ЗАГРУЗКЕ КЛАССА
    // Этот код выполняется ОДИН РАЗ при первой загрузке класса
    // Даже если вы создадите 100 объектов, этот блок выполнится только один раз
    // Почему static?
    // - Нам нужен только один экземпляр PrintStream на всю программу
    // - Не нужно создавать объект SafePrinter
    // - Можно вызывать SafePrinter.println() напрямую
    static {
        // СОЗДАЕМ НОВЫЕ PrintStream с кодировкой UTF-8
        // 1. System.out - оригинальный поток вывода, в который будет писать наш PrintStream
        // 2. true - autoFlush (автоматически сбрасывает буфер после каждого вызова)
        // 3. StandardCharsets.UTF_8.name() - явно указываем кодировку, charset name (кодировка для преобразования строк в байты)
        UTF8_OUT = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        UTF8_ERR = new PrintStream(System.err, true, StandardCharsets.UTF_8);
    }

    /**
     * Методы для вывода (аналоги System.out.println)
     * @param message сообщение для вывода
     */
    public static void println(String message) {
        UTF8_OUT.println(message); // Используем НАШ поток с UTF-8
    }

    /**
     * Методы для вывода (аналоги System.out.println)
     * @param format формат строки
     * @param args аргументы для форматирования
     */
    public static void printf(String format, Object... args) {
        UTF8_OUT.printf(format, args); // Используем НАШ поток с UTF-8
        UTF8_OUT.flush();  // Явный flush для printf
    }

    public static void print(Object obj) {
        UTF8_OUT.print(obj);
    }

    // Логирование с уровнем
    public static void debug(String message) {
        if (currentLevel.ordinal() <= Level.DEBUG.ordinal()) {
            log(Level.DEBUG, message);
        }
    }

    public static void info(String message) {
        if (currentLevel.ordinal() <= Level.INFO.ordinal()) {
            log(Level.INFO, message);
        }
    }

    public static void error(String message) {
        if (currentLevel.ordinal() <= Level.ERROR.ordinal()) {
            log(Level.ERROR, message);
        }
    }

    private static void log(Level level, String message) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String logMessage = String.format("[%s] [%s] %s",
                timestamp, level, message);

        if (level == Level.ERROR) {
            UTF8_ERR.println(logMessage);
        } else {
            UTF8_OUT.println(logMessage);
        }
    }

    /**
     * Методы для вывода (аналоги System.out.println)
     * @param message сообщение для вывода
     */
    public static void printError(String message) {
        UTF8_ERR.println("[ERROR] " + message); // Для ошибок тоже UTF-8
    }

    // Утилиты для форматирования
    public static void printSeparator() {
        println("========================================");
    }

    public static void printHeader(String header) {
        println("\n=== " + header + " ===");
    }

    // Сеттер
    public static void setLogLevel(Level level) {
        currentLevel = level;
    }

    // Геттер
    public static Level getLogLevel() {
        return currentLevel;
    }
}
