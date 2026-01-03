package tv.codealong.tutorials.various.strategy_example.one;

public class Main {
    public static void main(String[] args) {
        Worker worker = new Worker();
        worker.doWork(new FootJob());
        worker.doWork(new HandJob());
    }
}
