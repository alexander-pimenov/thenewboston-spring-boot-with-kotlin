package tv.codealong.tutorials.springboot.thenewboston.multithreading;

import java.util.ArrayList;
import java.util.List;

/**
 * Пример плохого кода, тут на один ресурс list может быть любое количество блокировок.
 */
public class _01_ContainerBad {
    public static final List<String> list = new ArrayList<>();

    synchronized void addEntry(String entry) {
        list.add(entry);
    }

//    synchronized String getEntry(int index) {
//        return list.get(index);
//    }
}
