package tv.codealong.tutorials.various.multithreading;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

import static tv.codealong.tutorials.various.multithreading._06_TransferWithDeadLock.transfer;

/**
 * Рассмотрим примеры dead lock.
 * Дедлок - это ситуация, когда два или более потоков ожидают друг друга для достижения синхронизации.
 * Чтобы избежать дедлока, нужно убедиться, что потоки всегда ожидают друг друга в одном и том же порядке.
 * И для этого есть 4 правила:
 * 1. Взаимное исключение (неразделяемые ресурсы).
 * 2. Минимум два ресурса (один держит, другой просит).
 * 3. Ресурс освобождается только добровольно тем, кто его держит.
 * 4. Направленный граф ожидания имеет цикл.
 * <br>
 * ArrayDeque - не потокобезопасный класс коллекции, которая работает как очередь.
 * <p>
 * В этом примере смотрим на круговорот строчек между очередями.
 * И в том случае мы довольно быстро придем в ситуацию мертвой блокировки (Deadlock). В графе блокировок "успешно"
 * реализуется цикл, т.к. потоки ожидают друг друга.
 */
public class _06_TransferDataDeadLockExample {
    public static void main(String[] args) throws InterruptedException {
        //создаем две очереди
        Queue<String> in = new ArrayDeque<>(Arrays.asList("foo", "bar", "baz"));
        Queue<String> out = new ArrayDeque<>(Arrays.asList("foo", "bar", "baz"));

        //создаем два потока
        //В этом потоке переливаем данные из очереди in в другую out
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                System.out.println("Thread1: " + i);
                transfer(in, out);
            }
        });

        //В этом потоке переливаем данные из очереди out в другую in
        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                System.out.println("Thread2: " + i);
                transfer(out, in);
            }
        });
        System.out.println("Started...");
        //запускаем потоки
        thread1.start();
        thread2.start();
        //ожидаем завершения потоков
        thread1.join();
        thread2.join();
        System.out.println("Finished!");
        //Started...
        //Thread2: 0
        //Thread1: 0
        //Thread2: 1
        //Thread1: 1
        //Thread1: 2
        //Thread2: 2
        //Thread2: 3
        //Thread2: 4
        //на какой-то итерации мы попадаем в мертвую блокировку и программа зависнет!
        //каждый из потоков перейдет в состояние BLOCKED и не сможет разблокироваться!
    }
}
