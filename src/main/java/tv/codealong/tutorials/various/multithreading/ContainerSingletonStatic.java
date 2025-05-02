package tv.codealong.tutorials.various.multithreading;

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
 * volatile - лучше писать, если мы используем Double-checked locking и хотим быть уверены, что если в цепочке ссылок на
 * объекты не будут использоваться не final поля, то всё равно поля будут инициализированы.
 */
public class ContainerSingletonStatic {
    private ContainerSingletonStatic() {
    }

    private static volatile ContainerSingletonStatic INSTANCE; //теперь это будет работать в многопоточном окружении (happens before)
//    private static ContainerSingleton INSTANCE;
    //private static final ContainerSingleton INSTANCE = new ContainerSingleton();

    //чтобы объект был не совсем бесполезным сделаем ему поле с публичным доступом,
    //чтобы мы могли его использовать.
    int x = 1;
    //final int x = 1;

    //Double-checked locking - блокировка с двойной проверкой.
    // Но этот способ окажется не рабочий для многопоточности, если не использовать слово volatile для поля INSTANCE.
    static ContainerSingletonStatic getInstance() {
        if (INSTANCE == null) {
            synchronized (ContainerSingletonStatic.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ContainerSingletonStatic();
                }
            }
        }
        return INSTANCE;
    }

    //lazy - работает для многопоточности, но имеет накладные расходы из-за блокировки synchronized при каждом вызове метода.
    static synchronized ContainerSingletonStatic getInstance1() {
            INSTANCE = new ContainerSingletonStatic();
        return INSTANCE;
    }

    //lazy thread-safe - работает для многопоточности, но имеет накладные расходы из-за блокировки synchronized при каждом вызове метода.
    static synchronized ContainerSingletonStatic getInstance2() {
        if (INSTANCE == null) {
            INSTANCE = new ContainerSingletonStatic();
        }
        return INSTANCE;
    }

}
