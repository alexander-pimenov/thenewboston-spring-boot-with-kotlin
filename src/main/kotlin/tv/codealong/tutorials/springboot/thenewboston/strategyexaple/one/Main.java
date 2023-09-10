package tv.codealong.tutorials.springboot.thenewboston.strategyexaple.one;

public class Main {
    public static void main(String[] args) {
        Worker worker = new Worker();
        worker.doWork(new FootJob());
        worker.doWork(new HandJob());
    }
}
