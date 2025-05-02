package tv.codealong.tutorials.various.strategyexaple.one;

public class Main {
    public static void main(String[] args) {
        Worker worker = new Worker();
        worker.doWork(new FootJob());
        worker.doWork(new HandJob());
    }
}
