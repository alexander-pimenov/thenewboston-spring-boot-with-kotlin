package tv.codealong.tutorials.springboot.thenewboston.multithreading;

/**
 * Пример, показывающий, что арифметические операции не атомарны.
 */
public class Counter {
    // Обычное поле. Не важно что здесь указывается слово volatile или нет.
    volatile int count = 0;
}
