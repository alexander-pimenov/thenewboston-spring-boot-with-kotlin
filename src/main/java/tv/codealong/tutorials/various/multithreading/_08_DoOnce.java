package tv.codealong.tutorials.various.multithreading;

/**
 * В методе runnable action хотим выполнить только один раз и не больше, т.е. при повторном
 * вызове action вызываться не должен.
 * Но этот метод не работает, т.к. могут одновременно два потока войти в метод и выполнить условие и вызвать
 * action два раза. Тут не спасает и слово volatile, т.к. все равно оба потока войдут в метод - doOnce.
 * Есть другой способ, это использовать volatile, double-checked locking и синхронизироваться только на условии -
 * смотри это в методе doOnceGood.
 */
public class _08_DoOnce {
    private volatile boolean flag = false;

    //Этот метод не работает, т.к. в него могут зайти одновременно оба потока и выполнить action.run()
    void doOnce(Runnable action) {
        if (!flag) {
            flag = true;
            action.run();
        }
    }

    //Можно использовать double-checked locking, и в блоке синхронизации делать только проверку, а не запуск действия.
    //В блоке synchronized нужно делать как можно меньше действий.
    //
    void doOnceGood(Runnable action) {
        if (!flag) {
            boolean set = false;
            synchronized (this) {
                if (!flag) {
                    set = true;
                    flag = true;
                }
            }
            if (set) {
                action.run();
            }
        }
    }
}
