package tv.codealong.tutorials.various.multithreading.traditional_and_virtual_thread;

public class ProblemTraditionalThreadDemo {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100_000; i++) {
            new Thread(() -> {
                System.out.println("Hello from thread " + Thread.currentThread().getName());
            }).start();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + (endTime - startTime) + " ms");
        stackOverFlowErrorExample();
    }

    public static void stackOverFlowErrorExample() {
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                try {
                    System.out.println("Hello from method stackOverFlowErrorExample, thread " + Thread.currentThread().getName());
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }
}
