package tv.codealong.tutorials.various.multithreading;

import java.util.concurrent.atomic.AtomicReference;

public class _09_AtomicPointGood {

    /**
     * Объект, который сдержит координаты точки.
     */
    private record Point(int x, int y) {
        Point rotateClockwise(){
            return new Point(y, -x);
        }
    }

    //AtomicReference работает как и AtomicInteger, но хранит ссылку на объект, т.е. работает со ссылочным полем.
    private final AtomicReference<Point> pt = new AtomicReference<>(new Point(0, 1));

    /**
     * Метод для поворота точки на 90 градусов по часовой стрелке.
     * Этот метод АТОМАРНЫЙ.
     */
    public void rotateClockwise() {
       pt.updateAndGet(Point::rotateClockwise);
    }
}
