package tv.codealong.tutorials.springboot.thenewboston.strategyexaple.one;

public class HandJob implements IJob {
    @Override
    public void doJob() {
        System.out.println("Hand Job ...");
    }
}
