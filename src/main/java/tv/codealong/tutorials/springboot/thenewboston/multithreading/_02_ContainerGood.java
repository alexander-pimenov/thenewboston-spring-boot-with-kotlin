package tv.codealong.tutorials.springboot.thenewboston.multithreading;

import java.util.ArrayList;
import java.util.List;

/**
 * Пример хорошего кода, тут на один ресурс list может быть только одна блокировка.
 * Теперь экземпляр ресурса list тоже привязан к экземпляру класса контейнера, который его использует.
 */
public class _02_ContainerGood {

    public final List<String> list = new ArrayList<>();

    synchronized void addEntry(String entry) {
        list.add(entry);
    }

//    synchronized String getEntry(int index) {
//        return list.get(index);
//    }

    synchronized int size() {
        return list.size();
    }
}
