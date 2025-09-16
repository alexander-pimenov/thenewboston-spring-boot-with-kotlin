package tv.codealong.tutorials.various.locks;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Пример использования Condition
 * Пример ReentrantCondExample демонстрирует использование объекта условия Condition с блокировкой ReentrantLock.
 * В примере описывается торговый склад, в который производитель завозит товар из списка GOODS. Товар регистрируется в
 * коллекции goods. Потребитель забирает товар со склада.
 * <p>
 * В конструкторе примера создаются торговый склад store и два потока : producer, consumer, исходный код которых
 * представлен ниже. Метод printMessage выводит сообщения потоков в консоль.
 */
public class ReentrantCondExample {
    Store store = null;
    SimpleDateFormat sdf = null;
    final String[] GOODS = {"Milk", "Kefir", "Ryazhenka", "Coffee", "Tea"};
    List<String> goods = new ArrayList<String>();

    ReentrantCondExample() {
        store = new Store();
        sdf = new SimpleDateFormat("HH:mm:ss  ");

        Thread producer = new Thread(new Producer());
        Thread consumer = new Thread(new Consumer());
        System.out.println("Start producer...");
        producer.start();
        System.out.println("Start consumer...");
        consumer.start();
        boolean producerAlive = producer.isAlive();
        System.out.println("producerAlive = " + producerAlive);
        boolean consumerAlive = consumer.isAlive();
        System.out.println("consumerAlive = " + consumerAlive);

        while (producer.isAlive() || consumer.isAlive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("\nCompleting the example");
        System.exit(0);

    }

    //-----------------------------------------------------
    void printMessage(final String msg) {
        if (msg != null) {
            String text = sdf.format(new Date()) + msg;
            System.out.println(text);
        } else
            System.out.println("\tGoods in stock: "
                    + goods.size());
    }

    class Consumer implements Runnable {
        public void run() {
            System.out.println("Consumer is running... with " + Thread.currentThread().getName());
            if (goods.isEmpty()) {
                System.out.println("#Consumer. Goods in stock: " + goods.size());
            }
            for (int i = 0; i < GOODS.length; i++) {
                try {
                    Thread.sleep(8000);
                } catch (InterruptedException ignored) {
                }
                store.get();
            }
        }
    }

    class Producer implements Runnable {

        public void run() {
            System.out.println("Producer is running... with " + Thread.currentThread().getName());
            for (String good : GOODS) {
                store.put(good);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    class Store {
        ReentrantLock lock;     // блокировка
        Condition cond;         // условие блокировки

        Store() {
            lock = new ReentrantLock();
            cond = lock.newCondition();
        }

        public void get() {
            System.out.println("#get - " + Thread.currentThread().getName());
            lock.lock();
            try {
                // ожидание на пустом складе
                while (goods.isEmpty()) {
                    System.out.println("#wait - " + Thread.currentThread().getName());
                    cond.await();
                }

                printMessage("Realization :" + goods.getFirst());
                goods.removeFirst();
                printMessage(null);
                // Сигнализация
                cond.signalAll();
            } catch (InterruptedException ignored) {
            } finally {
                lock.unlock();
            }
        }

        public void put(final String good) {
            System.out.println("#put - " + Thread.currentThread().getName());
            lock.lock();
            try {
                // ожидание освобождения места
                while (goods.size() >= 3)
                    cond.await();
                goods.add(good);

                printMessage("Delivery :" + good);
                printMessage(null);
                // Сигнализация
                cond.signalAll();
            } catch (InterruptedException e) {
            } finally {
                lock.unlock();
            }
        }
    }

    //-----------------------------------------------------
    public static void main(String[] args) {
        new ReentrantCondExample();
    }
}