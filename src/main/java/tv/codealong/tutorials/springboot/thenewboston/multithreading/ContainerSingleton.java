package tv.codealong.tutorials.springboot.thenewboston.multithreading;

/**
 * Сделаем правильный попoтокоразделяемый объект, который конструируется только один раз - синглтон.
 * Этот объект будет создаваться в момент инициализации класса (если поле INSTANCE будет иметь значение:
 * private static final ContainerSingleton INSTANCE = new ContainerSingleton();)
 * и может оказаться, что этот объект инициализировался на более раннем уровне и не связанного с вызовом метода getInstance().
 * Поэтому нужно сделать ленивую инициализацию (Lazy).
 * <p>
 * Если использовать слово synchronized, то синхронизация будет происходить на каждый вызов метода getInstance().
 * Это нам нужно только в самый первый момент инициализации объекта. Потом нам нужно возвращать уже имеющейся объект.
 * А synchronized несет накладные расходы при чтении уже инициализированной переменной.
 * <p>
 * Поэтому мы используем volatile для INSTANCE + double-checked locking - блокировка с двойной проверкой.
 */
public class ContainerSingleton {
    private ContainerSingleton() {
    }

    private static volatile ContainerSingleton INSTANCE; //теперь это будет работать в многопоточном окружении (happens before)
//    private static ContainerSingleton INSTANCE;
    //private static final ContainerSingleton INSTANCE = new ContainerSingleton();

    //чтобы объект был не совсем бесполезным сделаем ему поле с публичным доступом,
    //чтобы мы могли его использовать.
    int x = 1;

    //double-checked locking - блокировка с двойной проверкой.
    // Но этот способ окажется не рабочий для многопоточности, если не использовать слово volatile для поля INSTANCE.
    static ContainerSingleton getInstance() {
        if (INSTANCE == null) {
            synchronized (ContainerSingleton.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ContainerSingleton();
                }
            }
        }
        return INSTANCE;
    }

    //lazy - работает для многопоточности, но имеет накладные расходы из-за блокировки synchronized при каждом вызове метода.
    static synchronized ContainerSingleton getInstance1() {
            INSTANCE = new ContainerSingleton();
        return INSTANCE;
    }

    //lazy thread-safe - работает для многопоточности, но имеет накладные расходы из-за блокировки synchronized при каждом вызове метода.
    static synchronized ContainerSingleton getInstance2() {
        if (INSTANCE == null) {
            INSTANCE = new ContainerSingleton();
        }
        return INSTANCE;
    }

}
