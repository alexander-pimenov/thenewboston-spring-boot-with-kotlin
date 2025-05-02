package tv.codealong.tutorials.various.multithreading;

/**
 * Пример, показывающий порядок Java Memory Model.
 */
public class _04_FooExample {
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10000; i++) {
            Foo foo = new Foo();
            //В первом потоке мы записываем в поля объекта значения x=1 и y=1
            Thread t1 = new Thread(() -> {
                foo.x = 1; //plain write
                foo.y = 1; //volatile write
            });
            //Во втором потоке мы читаем эти поля объекта
            Thread t2 = new Thread(() -> {
                //Сначала дожидаемся в цикле пока в y не будет записано значение 1
                while (foo.y != 1) ;

                //Тут хорошо виден порядок (Java Memory Model):
                //запись в x происходит раньше записи в y
                //и т.к. мы читаем в t2 переменную x после того, как проверили y, а она была записана в t1
                //после x, то в t2 гарантированно будет прочитано значение 1 в x.
                System.out.println(foo.x);
                //Если бы мы не объявили переменную y как volatile, то это не было бы гарантированно!!!
            });
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        }
    }
}
