package tv.codealong.tutorials.springboot.thenewboston.multithreading;

import java.util.ArrayList;
import java.util.List;

public class _01_ContainerFixBadExample {
    public static void main(String[] args) throws InterruptedException {

        //создаем новый контейнер один раз в начале и передаем его всем потокам.
        _01_ContainerBad container = new _01_ContainerBad();

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

        //Ждем завершения потоков
        for (Thread thread : threads) {
            thread.join(); //дождаться завершения потока
        }

        //выводим размер списка, ожидаем 1_000_000, и выводится верно
        System.out.println("Size of list is " + _01_ContainerBad.list.size()); //Size of list is 1000000
    }
}
