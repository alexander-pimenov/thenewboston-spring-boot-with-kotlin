package tv.codealong.tutorials.various.sequenceAndStream

import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

/**
 * Для аннотации @State(Scope.Benchmark) в Java-библиотеке JMH (Java Microbenchmark Harness) вам не нужны специфические внешние зависимости, но требуется, чтобы класс, помеченный этой аннотацией, был частью вашего проекта, который вы компилируете и запускаете как бенчмарк. Сама библиотека JMH предоставляет все необходимые инструменты и зависимости для работы с этой аннотацией, включая зависимости для запуска тестов и обработки результатов.
 * JMH — это фреймворк для бенчмаркинга, а не одна из зависимостей, для которой нужно что-то подключать. Это набор библиотек, который сам по себе является «зависимостью» для вашего проекта.
 * Аннотация @State определяет жизненный цикл состояния, которое будет использоваться в тестах производительности. Scope.Benchmark означает, что одно и то же состояние будет создано и использоваться на протяжении всего бенчмарка, что необходимо для точного измерения производительности.
 * Зависимости для самого бенчмарка: Вашему проекту потребуется зависимость JMH для запуска тестов. Обычно это делается с помощью системы сборки, такой как Maven или Gradle, которые позволяют добавить необходимые зависимости JMH в ваш pom.xml или build.gradle. При этом в сборке проекта должны быть указаны зависимости для ядра JMH и его инструментов для запуска и обработки результатов.
 */
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class SequenceVsCollectionBenchmark {
    private val largeList = (1..1_000_000).toList()

    @Benchmark
    fun withCollection(): List<Int> {
        return largeList
            .filter { it % 2 == 0 }
            .map { it * 2 }
            .take(1000)
            .toList()
    }

    @Benchmark
    fun withSequence(): List<Int> {
        return largeList.asSequence()
            .filter { it % 2 == 0 }
            .map { it * 2 }
            .take(1000)
            .toList()
    }
}