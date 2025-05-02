package tv.codealong.tutorials.various.multithreading;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Создание потока это дорогостоящая операция и может занимать десятки-сотни микросекунд.
 * И в предыдущем примере {@link _08_DoOnceExample} пока создается второй поток, первый может уже отработать
 * и завершиться, и мы не сможем протестировать нормально метод doOnce.
 * <p>
 * Для решения этой проблемы можно использовать {@link CountDownLatch}.
 * CountDownLatch - это механизм синхронизации, который позволяет одному или нескольким потокам
 * ожидать, пока не будут завершены определенные условия или задачи, прежде чем продолжить свою работу.
 * Он работает на основе счетчика, который уменьшается при вызове метода countDown() и увеличивается
 * в других потоках при вызове метода await(). Значение счетчика устанавливается нами в конструкторе.
 * В каждом потоке мы ждем защелку, когда она дойдет до нуля.
 * Здесь это сделано для чтобы вначале все потоки запустились, одновременно. Так же мы можем увидеть
 * что все потоки отрабатывают практически одновременно.
 */

public class _08_DoOnceWithCountDownLatchExample {
    //Сколько потоков будет запущено в системе. Сколько ядер у процессора столько и потоков.
    static final int THREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) throws InterruptedException {
        _08_DoOnce doer = new _08_DoOnce();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger count = new AtomicInteger();
        Runnable runnable = () -> {
            try {
                latch.await();
            } catch (InterruptedException ignored) {
                //throw new RuntimeException(e);
            }
            doer.doOnce(() -> {
                System.out.println("In thread: " + Thread.currentThread().getName());
                count.incrementAndGet();
            });
        };

        List<Thread> threads = Stream.generate(() -> new Thread(runnable))
                .limit(THREADS)
                .peek(Thread::start)
                .toList();
        //отщелкиваем защелку.
        latch.countDown();
        for (Thread thread : threads) {
            thread.join();
        }
        if (count.get() != 1) {
            System.out.println("oops");
        }
        //In thread: Thread-7
    }
}
