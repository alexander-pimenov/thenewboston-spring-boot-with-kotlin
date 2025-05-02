package tv.codealong.tutorials.various.multithreading;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

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
public class _08_DoOnceWithCompareAndSetThroughUpdater {
    private volatile int flag = 0;

    private static final AtomicIntegerFieldUpdater<_08_DoOnceWithCompareAndSetThroughUpdater> FLAG_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(_08_DoOnceWithCompareAndSetThroughUpdater.class, "flag");

    /**
     * Этот метод работает. Использование AtomicIntegerFieldUpdater имеет меньшие накладные расходы, чем
     * AtomicBoolean, но имеет более сложную реализацию.
     *
     * @param action выполнить какое-то действие, если условие выполнилось.
     */
    void doOnce(Runnable action) {
        if (FLAG_UPDATER.compareAndSet(this, 0, 1)) {
            action.run();
        }
    }

    /**
     * Пример реализации метода getAndIncrement, который работает атомарно и не блокирует потоки. Просто для примера.
     * Аналогичный метод есть в классе AtomicInteger и там он еще более быстрый, т.к. оптимизирован на
     * уровне процессора, т.к. используется специальная команда (инструкция) для процессора для инкремента.
     */
    int getAndIncrement(AtomicInteger i) {
        int cur;
        do {
            cur = i.get();
        } while (!i.compareAndSet(cur, cur + 1));
        return cur;
    }

    /**
     * Пример реализации метода getAndDouble, который работает атомарно и не блокирует потоки. Основан
     * на примере метода getAndIncrement.
     */
    int getAndDouble(AtomicInteger i) {
        int cur;
        do {
            cur = i.get();
        } while (!i.compareAndSet(cur, cur * 2));
        return cur;
    }

    /**
     * Пример реализации метода getAndDouble через лямбду.
     */

    int getAndDoubleWithLambda(AtomicInteger i) {
        return i.getAndUpdate(val -> val * 2);
    }

}
