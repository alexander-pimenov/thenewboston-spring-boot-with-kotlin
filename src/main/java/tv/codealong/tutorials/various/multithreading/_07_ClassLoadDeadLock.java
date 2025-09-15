package tv.codealong.tutorials.various.multithreading;

import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

/**
 * Мы знаем, что загрузка классов осуществляется один раз (это гарантирует JVM).
 * Здесь показан пример загрузки класса, когда в блоке static используется параллельная загрузка классов (параллельный стрим).
 * <p>
 * Пример рассмотрен в статье https://stackoverflow.com/q/34820066/4856258
 * "Why does parallel stream with lambda in static initializer cause a deadlock?"
 */
public class _07_ClassLoadDeadLock {
    //В этом варианте используется анонимный класс IntUnaryOperator, он создастся, инициализируется, и в нём исполнится map,
    //всё загрузится хорошо.
    //Первым вызовется этот блок static
    static {
        int sum = IntStream.range(0, 10000)
                .parallel()
                .map(new IntUnaryOperator() {
                    @Override
                    public int applyAsInt(int x) {
                        return x * 2;
                    }
                })
                .sum();
        System.out.println("done: " + sum);
    }

    // Если использовать лямбду вместо анонимного класса, то программа зависнет, т.к. лямбда функция это приватный метод,
    // который создается внутри текущего класса ClassLoadDeadLock и чтобы вызвать этот метод, нужно чтобы класс
    // ClassLoadDeadLock был уже инициализирован, т.к. нельзя вызвать метод на не инициализированном классе.
    // А тут класс не может быть инициализирован до конца, т.к. инициализатор класса ждет пока эта параллельная операция .parallel()
    // завершится, а она не завершается, т.к. он ждет завершения этого класса и он ждет завершения этой операции.
    // Это пример дедлока.
    // Два класса ожидают друг друга.
    // Вторым вызовется этот блок static (но тут мы зависнем в дедлоке)
    static {
        int sum = IntStream.range(0, 10000)
                .parallel()
                .map(x -> x * 2)
                .sum();
        System.out.println("done: " + sum);
    }

    public static void main(String[] args) {
//  тут ничего не нужно писать, просто запустим этот метод и увидим, что программа зависнет на втором блоке static{}
    }
}
