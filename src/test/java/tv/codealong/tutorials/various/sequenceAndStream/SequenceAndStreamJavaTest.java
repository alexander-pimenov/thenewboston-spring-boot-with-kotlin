package tv.codealong.tutorials.various.sequenceAndStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static tv.codealong.tutorials.various.sequenceAndStream.SequenceAndStreamKtTestKt.getEmployees;

/**
 * ⚡ Ключевое отличие: Lazy vs Eager Evaluation
 * Stream - ленивые операции, но создает новый Stream на каждом шаге
 * Sequence - полностью ленивые вычисления, обрабатывает элементы по одному
 */
public class SequenceAndStreamJavaTest {
    @Test
    public void testSequenceAndStreamJava() {
        List<String> list = Arrays.asList("aaa1", "aa2", "bbb1", "cc2", "ccc1");
        List<String> result = list.stream()
                .filter(s -> s.length() > 3)
                .map(String::toUpperCase)
                .toList();
        System.out.println(result);
    }

    @DisplayName("1. Порядок выполнения операций. Stream (поэлементная обработка с промежуточными коллекциями)")
    @Test
    public void testSequenceAndStreamJava2() {
        List<String> list = Arrays.asList("a1", "a2", "b1", "c2", "c1");

        list.stream()
                .filter(s -> {
                    System.out.println("stage filter: " + s);
                    return s.startsWith("c");
                })
                .map(s -> {
                    System.out.println("stage map: " + s);
                    return s.toUpperCase();
                })
                .forEach(s -> System.out.println("stage forEach: " + s));
        //stage filter: a1
        //stage filter: a2
        //stage filter: b1
        //stage filter: c2
        //stage map: c2
        //stage forEach: C2
        //stage filter: c1
        //stage map: c1
        //stage forEach: C1
    }

    @Test
    @DisplayName("2. Производительность на больших данных")
    public void testSequenceAndStreamJava3() {
        // Метод IntStream.map(n -> n * 2) возвращает IntStream, а не Stream<Integer>.
        // Когда вы вызываете .collect(Collectors.toList()), компилятор ожидает Stream<T>,
        // но на этапе IntStream вы всё ещё работаете с примитивами int, а Collectors.toList() работает с объектами (Integer, а не int).
        // То есть, Collectors.toList() нельзя напрямую использовать на IntStream — нужно сначала упаковать (box) значения в объекты Integer.
        List<Integer> collect = IntStream.range(1, 1000_000)
                .filter(n -> n % 2 == 0)
                .map(n -> n * 2)
                .limit(10)
                .boxed()
                .collect(Collectors.toList());
        System.out.println(collect);
    }

    @Test
    @DisplayName("1. Простая группировка по отделу")
    public void testSimpleGrouping4() {
        Map<String, List<Employee>> byDepartment = getEmployees().stream()
                .collect(Collectors.groupingBy(Employee::getDepartment));
        System.out.println(byDepartment);
        //{
        // Engineering=[
        //      Employee(name=Alice, position=, department=Engineering, salary=5000),
        //      Employee(name=Bob, position=, department=Engineering, salary=6000),
        //      Employee(name=Eve, position=, department=Engineering, salary=5500)
        //      ],
        // Marketing=[
        //      Employee(name=Charlie, position=, department=Marketing, salary=4500),
        //      Employee(name=David, position=, department=Marketing, salary=4000)
        //      ]
        //}
    }

    @Test
    @DisplayName("2. Группировка с подсчетом количества")
    public void testSimpleGrouping5() {
        Map<String, Long> countByDept = getEmployees().stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.counting()));
        System.out.println(countByDept);
        //{Engineering=3, Marketing=2}
    }

    @Test
    @DisplayName("3. Группировка с агрегацией")
    public void testSimpleGrouping6() {
        // Средняя зарплата по отделам
        Map<String, Double> avgSalaryByDept = getEmployees().stream()
                .collect(Collectors.groupingBy(
                                Employee::getDepartment,
                                Collectors.averagingDouble(Employee::getSalary)
                        )
                );
        System.out.println(avgSalaryByDept);
        //{Engineering=5500.0, Marketing=4250.0}

        // Сумма зарплат по отделам
        Map<String, Integer> sumSalaryByDept = getEmployees().stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.summingInt(Employee::getSalary)
                ));
        System.out.println(sumSalaryByDept);
        //{Engineering=16500, Marketing=8500}

        // Минимальная зарплата по отделам
        Map<String, Optional<Employee>> minSalaryByDept = getEmployees().stream()
                .collect(Collectors.groupingBy(
                                Employee::getDepartment,
                                Collectors.minBy(Comparator.comparing(Employee::getSalary))
                        )
                );
        System.out.println(minSalaryByDept);
        //{
        // Engineering=Optional[
        //      Employee(name=Alice, position=, department=Engineering, salary=5000)
        //      ],
        // Marketing=Optional[
        //      Employee(name=David, position=, department=Marketing, salary=4000)
        //      ]
        //}

        // Группировка зарплат
        Map<String, List<Integer>> salaryByDept = getEmployees().stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.mapping(
                                Employee::getSalary, Collectors.toList()
                        )));
        System.out.println(salaryByDept);
        //{Engineering=[5000, 6000, 5500], Marketing=[4500, 4000]}

    }
}
