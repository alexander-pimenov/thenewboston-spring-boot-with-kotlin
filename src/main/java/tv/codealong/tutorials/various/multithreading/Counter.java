package tv.codealong.tutorials.various.multithreading;

/**
 * Пример, показывающий, что арифметические операции не атомарны.
 */
public class Counter {
    // Обычное поле. Не важно что здесь указывается слово volatile или нет.
    volatile int count = 0;
}
