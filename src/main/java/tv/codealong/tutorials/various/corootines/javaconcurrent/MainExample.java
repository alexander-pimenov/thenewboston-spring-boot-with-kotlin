package tv.codealong.tutorials.springboot.thenewboston.corootines.javaconcurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Многопоточность в Java
 * В Java многопоточность реализуется с помощью классов Thread и интерфейса Runnable.
 * Также есть более высокоуровневые инструменты, такие как ExecutorService и ForkJoinPool.
 * Основные понятия:
 * Thread — поток выполнения.
 * Runnable — задача, которая может быть выполнена в потоке.
 * ExecutorService — пул потоков для управления множеством задач.
 * Future — объект, который представляет результат асинхронной операции.
 */
public class MainExample {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        System.out.println("===An example of a simple Thread===");
        Thread thread = new Thread(() -> {
            System.out.println("Hello from thread: " + Thread.currentThread().getName());
        });
        thread.start();
        System.out.println("Hello from main thread: " + Thread.currentThread().getName());


        System.out.println("===An example of using an ExecutorService thread pool to manage multiple tasks===");
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            System.out.println("Task 1 executed by " + Thread.currentThread().getName());
        });

        executor.submit(() -> {
            System.out.println("Task 2 executed by " + Thread.currentThread().getName());
        });
        executor.shutdown();

        System.out.println("===Using the Future to get the result===");
        ExecutorService executor1 = Executors.newSingleThreadExecutor();
        Future<String> futureResult = executor1.submit(() -> {
            Thread.sleep(3000L);
            return "Hello, World (it's from future)!";
        });

        System.out.println(futureResult.get()); // Ожидаем результат
        executor1.shutdown();
    }
}
