package tv.codealong.tutorials.various.corootines.javaconcurrent.completablefuture;

import java.util.concurrent.CompletableFuture;

/**
 * CompletableFuture — это мощный инструмент в Java для асинхронного программирования,
 * который позволяет удобно работать с фоновыми задачами без блокировки основного потока.
 * Он похож на колбэки, но гораздо удобнее и гибче.
 * <p>
 * 🔹 Простыми словами:
 * CompletableFuture — это "обещание" (promise), что в будущем будет результат (или ошибка).
 * <p>
 * Ты запускаешь операцию (например, запрос к API или БД) в фоне.
 * Пока она выполняется, основной поток может делать другие дела.
 * Когда результат готов, ты можешь его обработать (например, вывести на экран или передать дальше).
 * <p>
 * 📌 Аналогия:
 * Ты заказываешь еду через приложение:
 * Ты не стоишь у двери и не ждёшь курьера (не блокируешь поток).
 * Когда еду привезли, тебе приходит уведомление (вызывается твой обработчик).
 * <p>
 * CompletableFuture vs Колбэки
 * Колбэки	CompletableFuture
 * Может быть Callback Hell (много вложенных колбэков)	Чистый и читаемый цепочный код
 * Сложно комбинировать несколько асинхронных операций	Легко объединять через thenCombine, thenCompose и др.
 * Нет встроенной обработки ошибок	Есть exceptionally, handle
 * <p>
 *  Где используется?
 * - Запросы к API / БД (не блокируя основной поток).
 * - Параллельные вычисления (например, обработать два запроса одновременно и объединить результат).
 * - Реактивное программирование (часто используется вместе с Spring WebFlux, Project Reactor).
 * <p>
 * 🔹 Плюсы CompletableFuture
 * ✅ Чище и удобнее, чем колбэки.
 * ✅ Можно комбинировать множество асинхронных операций.
 * ✅ Есть обработка ошибок.
 * ✅ Работает с ForkJoinPool (оптимизирован для асинхронных задач).
 * <p>
 * 🔹 Минусы
 * ❌ Сложнее для новичков (по сравнению с синхронным кодом).
 * ❌ Если переусердствовать, код может стать запутанным.
 * <p>
 * Вывод
 * CompletableFuture — это современный способ писать асинхронный код в Java без "ада колбэков". Он позволяет:
 * Запускать задачи в фоне.
 * Строить цепочки обработки.
 * Комбинировать результаты.
 * Обрабатывать ошибки.
 */
public class SimpleExample {
    public static void main(String[] args) {
        //1) Простой CompletableFuture
        // Запускаем асинхронную задачу
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000); // Имитация долгой операции (2 сек)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Результат операции";
        });

        // Подписываемся на результат (колбэк)
        future.thenAccept(result -> {
            System.out.println("Получен результат: " + result);
        });

        System.out.println("Ждём результат..."); // Выполнится сразу

        //Ждём результат...
        //Получен результат: Результат операции

        //2) Цепочка задач (chaining)
        //Можно объединять несколько асинхронных операций в цепочку:
        //Что происходит:
        //supplyAsync — запускает задачу в фоне.
        //thenApply — преобразует результат (асинхронно).
        //thenAccept — обрабатывает финальный результат.
        CompletableFuture.supplyAsync(() -> "Hello")
                .thenApply(s -> s + " World")       // "Hello World"
                .thenApply(String::toUpperCase)     // "HELLO WORLD"
                .thenAccept(System.out::println);   // Выведет "HELLO WORLD"

        //3) Комбинирование двух Future
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "World");

        // Объединяем два Future
        future1.thenCombine(future2, (s1, s2) -> s1 + " " + s2)
                .thenAccept(System.out::println);  // "Hello World"


        //4) Пример с ошибкой (Обработка ошибок)
        //Если возникнет ошибка, выполнится exceptionally.
        //Если нет — выполнится thenAccept.
        CompletableFuture.supplyAsync(() -> {
            if (Math.random() > 0.5) {
                throw new RuntimeException("Ошибка!");
            }
            return "Успех";
        }).exceptionally(ex -> {
            System.err.println("Ошибка: " + ex.getMessage());
            return "Запасной вариант";
        }).thenAccept(System.out::println);

    }
}
