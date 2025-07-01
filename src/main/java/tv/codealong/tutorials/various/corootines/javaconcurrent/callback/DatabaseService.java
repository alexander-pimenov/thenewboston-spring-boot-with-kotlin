package tv.codealong.tutorials.various.corootines.javaconcurrent.callback;

public class DatabaseService {
    public void fetchData(CallBack callBack) {
        // Simulate a long running task
        // Имитация долгой операции (например, запрос к БД)
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Ждём 5 секунд, Simulate a delay
                // Вызываем колбэк с результатом, т.к. данные из БД получены, мы вызываем метод onComplete
                callBack.onComplete("Данные из БД.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
