package tv.codealong.tutorials.various.multithreading;

import java.util.concurrent.atomic.AtomicInteger;

public class _09_AtomicPointBad {
    private final AtomicInteger x = new AtomicInteger(0);
    private final AtomicInteger y = new AtomicInteger(1);

    /**
     * Метод для поворота точки на 90 градусов по часовой стрелке.
     * Внимание! Этот метод НЕ АТОМАРНЫЙ и не обеспечивает никакой синхронизации!
     * Использовать его нужно только для примера.
     * В качестве примера можно использовать метод {@link _09_AtomicPointGood#rotateClockwise()}.
     * Т.к. сперва читается y, потом обновляется x, и потом обновляется y.
     */
    public void rotateClockwise() {
        int oldX = x.getAndSet(y.get());
        y.set(-oldX);
    }
}
