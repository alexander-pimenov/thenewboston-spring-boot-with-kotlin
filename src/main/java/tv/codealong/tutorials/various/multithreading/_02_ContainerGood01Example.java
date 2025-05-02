package tv.codealong.tutorials.various.multithreading;

import java.util.ArrayList;
import java.util.List;

public class _02_ContainerGood01Example {
    public static void main(String[] args) throws InterruptedException {
        //создаем новый контейнер
        _02_ContainerGood container = new _02_ContainerGood();

        Runnable foo = () -> {
            for (int i = 0; i < 100000; i++) {
                container.addEntry("foo: " + i);
            }
        };

        List<Thread> threads = new ArrayList<>();
        for (int i = 10; i > 0; i--) {
            Thread thread = new Thread(foo);
            thread.start(); //запустить thread
            threads.add(thread);
        }

        System.out.println("Size of list is " + container.size());
        //Мы здесь не джоиним потоки, а хотим дождаться, когда их количество станет = 1_000_000
        while (container.size() < 1_000_000) {}
        System.out.println("Size of list is " + container.size());
        System.out.println("Finished!");

        //        //Ждем завершения потоков
//        for (Thread thread : threads) {
//            thread.join(); //дождаться завершения потока
//        }


        //Если у нас метод чтения не будет также synchronized, то JIT-компилятор может его оптимизировать и вставить в
        // цикл true и тогда не будет работать проверка цикла и программа не завершится.
        //int size$0 = container.list.size;
        //if(size$0 < 1_000_000) {
        // while (true) {}
        //}
        //System.out.println("Finished!");
        //

    }
}
