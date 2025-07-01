package tv.codealong.tutorials.various.corootines.javaconcurrent.callback;

/**
 * Что такое колбэк (callback)?
 * Колбэк (callback) — это функция (или метод), которая передаётся в другую функцию (или метод)
 * и вызывается после выполнения какой-то асинхронной (или долгой) операции.
 * 📌 Простая аналогия:
 * Представь, что ты заказал пиццу и оставил свой номер телефона. Пока пицца готовится,
 * ты можешь заниматься своими делами. Когда пицца будет готова, тебе перезвонят (это и есть колбэк).
 * <p>
 * Плюсы колбэков:
 * ✔ Позволяют не блокировать основной поток (например, поток обработки HTTP-запросов).
 * ✔ Удобны для асинхронных операций (запросы к БД, API, файловые операции).
 * <p>
 * Минусы колбэков:
 * ❌ Может возникнуть "Callback Hell" (много вложенных колбэков, код становится нечитаемым).
 * ❌ Сложнее отлаживать, чем синхронный код.
 * Что используют вместо колбэков в современном Java?
 * CompletableFuture (более удобный асинхронный API).
 * <p>
 * Реактивные библиотеки (RxJava, Project Reactor).
 * <p>
 * Корутины (в Kotlin).
 * <p>
 * Но колбэки до сих пор используются, особенно в старых и высокопроизводительных фреймворках (Netty, Vert.x).
 */
public class MainCallbackExample {
    public static void main(String[] args) {
        DatabaseService databaseService = new DatabaseService();

        //1. Передаем анонимный класс в метод
        // Передаем м (объект CallBack) в метод, тут видим, как передается анонимный класс
//        databaseService.fetchData(new CallBack() {
//            @Override
//            public void onComplete(String data) {
//                System.out.println("Данные получены: " + data);
//            }
//        });

        //2. Передаем лямбду в метод
        // Передаем callBack (объект CallBack) в метод в виде лямбды:
        databaseService.fetchData(resultData -> {
            System.out.println("Данные получены: " + resultData);
        });

        System.out.println("Ждем данные...");
    }
}
