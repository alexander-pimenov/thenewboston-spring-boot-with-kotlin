package tv.codealong.tutorials.various.multithreading;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * В методе runnable action хотим выполнить только один раз и не больше, т.е. при повторном
 * вызове action вызываться не должен.
 * - compareAndSet - это базовый примитив для многих lock-free и wait-free алгоритмов. Т.е. мы не хотим использовать
 * синхронизацию и блокировку, которые дорогие, но хотим получить разумный результат.
 * Операция compareAndSet() является атомарной. Она поддерживается процессорами.
 * Она выполняет два действия: проверяет, что значение в ячейке соответствует ожидаемому (expected), и если это так,
 * то присваивает новое значение (update), и возвращает true. Если значение в ячейке не соответствует ожидаемому,
 * то операция не выполняется, т.е. возвращает false.
 */
public class _08_DoOnceWithCompareAndSet {
    // Используем AtomicBoolean, это специальный класс, у объекта которого есть метод compareAndSet
    private final AtomicBoolean flag = new AtomicBoolean(false);

    /**
     * Этот метод работает.
     * compareAndSet работает как synchronized, но работает быстрее, т.к. синхронизирует только на условие.
     * compareAndSet не блокирует потоки, а проверяет условие и выполняет действие, если условие выполнилось.
     * compareAndSet:expect - то, какое значение мы ожидаем прочитать. И в нашем случе это значение false.
     * compareAndSet:update - то, на какое значение мы хотим изменить, в случае если ожидаемое значение совпало с тем
     * что мы ожидаем. И в нашем случе мы хотим изменить на значение true.
     * @param action выполнить какое-то действие, если условие выполнилось.
     */
    void doOnce(Runnable action) {
        if (flag.compareAndSet(false, true)) {
            action.run();
        }
    }
}
