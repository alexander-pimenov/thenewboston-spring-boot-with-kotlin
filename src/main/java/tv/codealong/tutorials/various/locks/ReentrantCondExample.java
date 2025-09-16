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
 * <p>
 * - Производитель представлен внутренним классом Producer. Время задержки в методе run класса определяет возможный
 * интервал доставки товара.<br>
 * - Потребитель представлен классом Consumer. Время задержки в методе run класса определяет возможный интервал
 * реализации товара.<br>
 *
 * Производитель завозит товар чаще (время задержки меньше), чем потребитель забирает товар (время задержки больше).
 * На складе всего 3 места для товара. Таким образом, работу производителя необходимо притормаживать.
 * Эту функцию выполняет блокировка lock с условием cond в классе Store.
 * <p>
 * Класс Store включает метод получения товара put и метод выдачи товара get. Шаблон выполнения операций обоих методов
 * идентичен: сначала метод получает блокировку, чтобы другой поток не вошел в данный метод; после этого выполняется
 * проверка условий. Если условия не соблюдаются, то поток переводится в стадию ожидания методом cond.await().
 * Для второго потока условия должны быть соблюдены (такая бизнес-логика работы склада), и он должен выполнить свою
 * операцию. После этого второй поток «будит» первый, а сам завершает операцию и разблокирует метод.
 * «Разбуженный» первый поток вновь выполняет проверку условия, и далее действует согласно предписанному сценарию
 * (либо продолжение выполнения, либо переход в ожидание).
 * <p>
 * Выполнение примера:
 * Сообщения в консоли показывают, что сначала идет наполнение склада до 3-х единиц товара поскольку время задержки
 * Producer (2000 мс) значительно меньше времени задержки Consumer (8000 мс). При полном наполнении склада Producer
 * останавливается в ожидании освобождения склада. Как только со склада забирается товар, т.е. освобождается место,
 * сразу же производитель завозит новый товар и снова переходит в режим ожидания. Таким образом, видим, что блокировка
 * с условием работает точно так, как ей предписано документацией.
 *
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