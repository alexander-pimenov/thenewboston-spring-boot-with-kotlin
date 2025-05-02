package tv.codealong.tutorials.various.multithreading;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * Создание потоков управляемым способом, когда мы сами контролируем сколько будет создаваться потоков и
 * мы не в ручную их создаем и убиваем, а делегируем эту операцию пулу потоков.
 * <p>
 * это дорогостоящая операция и может занимать десятки-сотни микросекунд.
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

public class _10_ExecutorsExample {
    //Сколько потоков будет запущено в системе. Сколько ядер у процессора столько и потоков.
    static final int THREADS = Runtime.getRuntime().availableProcessors();
    private static final int JUMPS = 1000;

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        /**
         * В AtomicReference хранится наш объект {@link _08_DoOnce}, который мы хотим создать один раз.
         */
        AtomicReference<_08_DoOnce> doer = new AtomicReference<>();
        AtomicInteger count = new AtomicInteger();
        Runnable runnable = () -> {
            doer.get().doOnce(count::incrementAndGet);
        };
        // Создаем пул потоков. THREADS количество потоков. И в него передаем задачи (объект Runnable).
        // Каждый поток выполняет задачу Runnable. Мы получаем список объектов типа Future,
        // которые представляют результаты выполнения задач (когда-нибудь задачи будут выполнены).
        // Затем мы ждем завершения всех задач.
        // Future инкапсулирует в себе результат выполнения задачи.
        //future.get(); - блокирующий метод. Он ждет завершения выполнения задачи и возвращает результат.
        //Т.к. в нашем примере мы используем Runnable, то результат выполнения задачи будет null.
        //Если бы нам нужен был результат, то нам нужно использовать объект типа Callable, который возвращает результат.
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        for (int i = 0; i < JUMPS; i++) {
            count.set(0);
            doer.set(new _08_DoOnce());
            List<? extends Future<?>> futures = Stream.generate(() -> executor.submit(runnable))
                    .limit(THREADS)
                    .toList();
            for (Future<?> future : futures) {
                future.get();
            }
            if (count.get() != 1) {
                System.out.println("oops");
            }
        }
        // Завершаем работу пула потоков. Когда потоки нам уже не нужны их нужно уничтожить, т.к. это
        //ресурс. И не ждать когда до них доберется Garbage Collector.
        executor.shutdown();

    }
}
