package tv.codealong.tutorials.springboot.thenewboston.strategyexaple.one;

public class FootJob implements IJob {
    @Override
    public void doJob() {
        System.out.println("Foot Job ...");
    }
}
