package tv.codealong.tutorials.various.multithreading;

import java.util.List;
import java.util.stream.Stream;

/**
 * Создание потока это дорогостоящая операция и может занимать десятки-сотни микросекунд.
 * И в данном примере пока создается второй поток, первый может уже отработать и завершиться, и
 * мы не сможем протестировать нормально метод doOnce.
 * <p>
 * https://youtu.be/5YLA29EybMo?t=4124
 */

public class _08_DoOnceExample {
    //Сколько потоков будет запущено в системе. Сколько ядер у процессора столько и потоков.
    static final int THREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) throws InterruptedException {
        _08_DoOnce doer = new _08_DoOnce();
        Runnable runnable = () -> {
            doer.doOnce(() -> System.out.println("In thread: " + Thread.currentThread().getName()));
        };

        List<Thread> threads = Stream.generate(() -> new Thread(runnable))
                .limit(THREADS)
                .peek(Thread::start)
                .toList();

        for (Thread thread : threads) {
            thread.join();
        }
        //In thread: Thread-7
    }
}
