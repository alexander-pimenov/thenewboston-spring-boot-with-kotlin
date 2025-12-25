package tv.codealong.tutorials.various.tuple;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TupleTest {

    @Test
    public void testTuple() {

        var testTuple = new Tuple2<Integer, String>(100, "Hello");
        System.out.println(testTuple._1); //100
        System.out.println(testTuple._2); //Hello

        Tuple2<String, Integer> testTuple2 = Tuple.of("World", 1000);
        Tuple2<String, Integer> testTuple3 = Tuple.of("World", 1000);
        System.out.println(testTuple2.equals(testTuple3)); //true

        //нельзя изменять, но можно создать копию
        Tuple2<String, Integer> tuple4 = testTuple3.map2(value -> value + 11);
        System.out.println(testTuple3); //(World, 1000)
        System.out.println(tuple4); //(World, 1011)

        //можно изменять сразу два поля
        Tuple2<String, Integer> tuple5 = testTuple3.map((s, i) -> Tuple.of(s + "!", i + 1000));
        System.out.println(tuple5); //(World!, 2000)

        //можно изменять сразу два поля по отдельности
        Tuple2<String, Integer> tuple6 = testTuple3.map(
                s -> s.toUpperCase(Locale.ROOT), //первое поле
                i -> i + 123 //второе поле
        );
        System.out.println(tuple6); //(WORLD, 1123)

        //деструктуризация в стиле функционального программирования
        String message = testTuple2.apply((s, i) -> s + "___" + i);
        System.out.println(message); //World___1000

        //в таплах можно хранить и списки и мапы
        Tuple2<List<Integer>, Map<String, Integer>> listMapTuple2 = Tuple.of(Arrays.asList(1, 2, 3, 4, 5), Map.of("a", 1, "b", 2));
        System.out.println(listMapTuple2._1); //[1, 2, 3, 4, 5]
        System.out.println(listMapTuple2._2); //{b=2, a=1}
    }
}
