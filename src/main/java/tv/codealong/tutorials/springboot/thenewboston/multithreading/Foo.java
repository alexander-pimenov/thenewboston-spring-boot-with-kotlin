package tv.codealong.tutorials.springboot.thenewboston.multithreading;

/**
 * Пример, показывающий, что арифметические операции не атомарны.
 */
public class Foo {
    // Обычное поле. Не важно что здесь указывается слово volatile или нет.
    int x = 0;
    volatile int y = 0;
}
