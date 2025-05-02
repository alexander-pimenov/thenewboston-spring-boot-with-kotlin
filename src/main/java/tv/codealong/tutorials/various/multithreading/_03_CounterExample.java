package tv.codealong.tutorials.various.multithreading;

import java.util.List;
import java.util.stream.Stream;

public class _03_CounterExample {
    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();
        Runnable foo = () -> {
            for (int i = 0; i < 1000000; i++) {
                counter.count++;
            }
        };

        List<Thread> threads = Stream.generate(() -> new Thread(foo))
                .limit(10)
                .peek(Thread::start)
                .toList();

        for (Thread thread : threads) {
            thread.join();
        }

        //считаем количество, и ожидаем, что будет 10_000_000, но выводится меньше
        System.out.println(counter.count); //3987300
    }
}
