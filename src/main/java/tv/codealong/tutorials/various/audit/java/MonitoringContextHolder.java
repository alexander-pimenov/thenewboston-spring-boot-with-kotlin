package tv.codealong.tutorials.various.audit.java;

/**
 * Разберём по шагам, что происходит в этом классе, особенно с учётом **статических полей**, **статического блока инициализации** и методов.
 *
 * ---
 *
 * ## **1. Статическое поле `strategy`**
 * ```java
 * private static MonitoringContextHolderStrategy strategy;
 * ```
 * - Это **статическое** поле, которое принадлежит **классу**, а не отдельным объектам.
 * - Оно инициализируется при загрузке класса в JVM.
 * - По умолчанию имеет значение `null`, пока не будет проинициализировано.
 *
 * ---
 *
 * ## **2. Конструктор (нестатический)**
 * ```java
 * public MonitoringContextHolder() {
 * }
 * ```
 * - Конструктор пустой, так как класс не содержит нестатических полей.
 * - Создание экземпляра этого класса (`new MonitoringContextHolder()`) не влияет на статическое поле `strategy`.
 *
 * ---
 *
 * ## **3. Статический метод `initialize()`**
 * ```java
 * private static void initialize() {
 *     initializeStrategy();
 * }
 * ```
 * - Это **вспомогательный приватный метод**, который вызывает `initializeStrategy()`.
 * - Нужен для удобства, если в будущем инициализация усложнится.
 *
 * ---
 *
 * ## **4. Статический метод `initializeStrategy()`**
 * ```java
 * private static void initializeStrategy() {
 *     strategy = new GlobalMonitoringContextHolderStrategy();
 * }
 * ```
 * - Здесь происходит **инициализация статического поля `strategy`**.
 * - Создаётся экземпляр `GlobalMonitoringContextHolderStrategy` (предположительно, это реализация интерфейса `MonitoringContextHolderStrategy`).
 *
 * ---
 *
 * ## **5. Статический блок инициализации (`static {}`)**
 * ```java
 * static {
 *     initialize();
 * }
 * ```
 * - **Статический блок выполняется один раз при загрузке класса** в JVM (до создания любого объекта этого класса).
 * - В данном случае он вызывает `initialize()`, которая, в свою очередь, вызывает `initializeStrategy()`, инициализируя `strategy`.
 * - Это гарантирует, что `strategy` будет проинициализирована **до любого использования класса**.
 *
 * ---
 *
 * ## **6. Публичные статические методы для работы с контекстом**
 * Эти методы позволяют управлять контекстом (`MonitoringContext`) через текущую стратегию (`strategy`):
 *
 * ### **`clearContext()`**
 * ```java
 * public static void clearContext() {
 *     strategy.clearContext();
 * }
 * ```
 * - Очищает текущий контекст, делегируя вызов стратегии.
 *
 * ### **`getContext()`**
 * ```java
 * public static MonitoringContext getContext() {
 *     return strategy.getContext();
 * }
 * ```
 * - Возвращает текущий контекст из стратегии.
 *
 * ### **`setContext(MonitoringContext context)`**
 * ```java
 * public static void setContext(MonitoringContext context) {
 *     strategy.setContext(context);
 * }
 * ```
 * - Устанавливает новый контекст через стратегию.
 *
 * ### **`getContextHolderStrategy()`**
 * ```java
 * public static MonitoringContextHolderStrategy getContextHolderStrategy() {
 *     return strategy;
 * }
 * ```
 * - Возвращает текущую стратегию (может использоваться для расширения функциональности).
 *
 * ### **`createEmptyContext()`**
 * ```java
 * public static MonitoringContext createEmptyContext() {
 *     return strategy.createEmptyContext();
 * }
 * ```
 * - Создаёт новый пустой контекст через стратегию.
 *
 * ---
 *
 * ## **7. Переопределённый `toString()` (нестатический)**
 * ```java
 * @Override
 * public String toString() {
 *     return "MonitoringContextHolder[strategy='" + strategy.getClass().getSimpleName() + "']";
 * }
 * ```
 * - Возвращает строковое представление объекта, включая имя класса текущей стратегии.
 * - **Нестатический метод**, но обращается к статическому полю `strategy`.
 *
 * ---
 *
 * ## **Как это работает?**
 * 1. **При загрузке класса `MonitoringContextHolder` в JVM:**
 *    - Выполняется **статический блок `static {}`** → вызывается `initialize()` → `initializeStrategy()` → `strategy = new GlobalMonitoringContextHolderStrategy()`.
 *    - Теперь `strategy` готова к использованию.
 *
 * 2. **При вызове статических методов (`getContext()`, `setContext()`, и т. д.):**
 *    - Они работают с уже проинициализированной `strategy`.
 *
 * 3. **Если создать экземпляр класса (`new MonitoringContextHolder()`):**
 *    - Это не влияет на `strategy`, так как она статическая.
 *    - Можно вызвать `toString()`, который покажет текущую стратегию.
 *
 * ---
 *
 * ## **Вывод**
 * - Класс реализует **шаблон "Holder"** (хранение контекста через стратегию).
 * - **Статическая инициализация** гарантирует, что `strategy` будет готова до любого использования.
 * - Все методы **статические**, поэтому работают без создания экземпляра класса.
 * - Подходит для **хранения глобального состояния** (например, контекста мониторинга в приложении).
 *
 * Если бы `strategy` была **нестатической**, каждый экземпляр имел бы свою копию, что в данном случае не нужно.
 *
 * Если создать несколько экземпляров класса `MonitoringContextHolder` через `new`, это **не повлияет на статическое поле `strategy`**, потому что статические поля и методы принадлежат **классу**, а не отдельным объектам.
 *
 * ### **Что произойдёт при создании нескольких объектов?**
 * 1. **Статическое поле `strategy` общее для всех экземпляров**
 *    - Оно инициализируется **один раз** при загрузке класса (в статическом блоке `static {}`).
 *    - Даже если создать 100 объектов `MonitoringContextHolder`, `strategy` останется той же самой.
 *
 * 2. **Нестатические методы (например, `toString()`) используют общую `strategy`**
 *    - Поскольку `toString()` обращается к статическому полю `strategy`, все объекты будут показывать одно и то же значение.
 *
 * 3. **Конструктор пустой и не влияет на `strategy`**
 *    - В данном классе конструктор не делает ничего, поэтому создание новых объектов — просто выделение памяти под пустой экземпляр.
 *
 * ---
 *
 * ### **Пример**
 * ```java
 * public class Main {
 *     public static void main(String[] args) {
 *         MonitoringContextHolder holder1 = new MonitoringContextHolder();
 *         MonitoringContextHolder holder2 = new MonitoringContextHolder();
 *
 *         System.out.println(holder1); // MonitoringContextHolder[strategy='GlobalMonitoringContextHolderStrategy']
 *         System.out.println(holder2); // MonitoringContextHolder[strategy='GlobalMonitoringContextHolderStrategy']
 *
 *         // Меняем стратегию (статически)
 *         MonitoringContextHolder.setContext(new CustomMonitoringContext());
 *
 *         System.out.println(holder1); // Изменится у всех!
 *         System.out.println(holder2); // MonitoringContextHolder[strategy='GlobalMonitoringContextHolderStrategy']
 *     }
 * }
 * ```
 * #### **Вывод:**
 * ```
 * MonitoringContextHolder[strategy='GlobalMonitoringContextHolderStrategy']
 * MonitoringContextHolder[strategy='GlobalMonitoringContextHolderStrategy']
 * MonitoringContextHolder[strategy='GlobalMonitoringContextHolderStrategy']
 * MonitoringContextHolder[strategy='GlobalMonitoringContextHolderStrategy']
 * ```
 * (Если бы `setContext` менял стратегию, это отразилось бы во всех объектах.)
 *
 * ---
 *
 * ### **Выводы**
 * 1. **Создание нескольких `MonitoringContextHolder` бессмысленно**, так как класс работает только со **статическими** методами и полями.
 * 2. **Все экземпляры разделяют одно состояние `strategy`**.
 * 3. **Этот класс реализует паттерн Singleton "на минималках"** (хотя без защиты от создания экземпляров).
 * 4. **Если нужен настоящий Singleton**, лучше сделать:
 *    - Приватный конструктор
 *    - Статический метод `getInstance()`
 *    - Инициализацию через `enum` или `Holder`
 *
 * Но в данном случае класс задуман как **утилитарный (статический)**, и создание объектов не имеет смысла.
 */
public class MonitoringContextHolder {
    private static MonitoringContextHolderStrategy strategy;

    public MonitoringContextHolder() {

    }

    private static void initialize() {
        initializeStrategy();
    }

    private static void initializeStrategy() {
        strategy = new GlobalMonitoringContextHolderStrategy();
    }

    public static void clearContext() {
        strategy.clearContext();
    }

    public static MonitoringContext getContext() {
        return strategy.getContext();
    }

    public static void setContext(MonitoringContext context) {
        strategy.setContext(context);
    }

    public static MonitoringContextHolderStrategy getContextHolderStrategy() {
        return strategy;
    }

    public static MonitoringContext createEmptyContext() {
        return strategy.createEmptyContext();
    }

    @Override
    public String toString() {
        return "MonitoringContextHolder[strategy='" + strategy.getClass().getSimpleName() + "']";
    }

    static {
        initialize();
    }
}
