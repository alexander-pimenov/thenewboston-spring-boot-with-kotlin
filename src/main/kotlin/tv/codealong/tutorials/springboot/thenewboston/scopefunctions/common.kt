package tv.codealong.tutorials.springboot.thenewboston.scopefunctions

import kotlin.random.Random

/**
 * Чтобы помочь вам выбрать правильную функцию области действия для ваших целей, мы предлагаем вам таблицу, в которой обобщены основные различия между ними.
 *
 * Here's a table summarizing the key differences between the scope functions1:
 * -----------------------------------------------
 * Scope Function |	Context Object | Returns | Common Use Case | Is extension function
 * -----------------------------------------------
 * let | it | Lambda result |  Null checks, transformations | Yes
 * run | this |	Lambda result |	Object initialization, calculations | Yes
 * with | this | Lambda result |	Grouping multiple actions | No: takes the context object as an argument.
 * apply | this | The object itself |	Object configuration | Yes
 * also | it |	The object itself |	Additional actions, side-effects | Yes
 * -----------------------------------------------
 *
 * Вот краткое руководство по выбору функций области действия в зависимости от предполагаемой цели:
 * * Выполнение лямбда-выражения для ненулевых объектов:let
 * * Введение выражения в качестве переменной в локальной области видимости:let
 * * Конфигурация объекта:apply
 * * Конфигурация объекта и вычисление результата:run
 * * Выполнение операторов, где требуется выражение: не расширение run
 * * Дополнительные эффекты:also
 * * Группировка вызовов функций для объекта:with
 * <p>
 *  Существует два основных различия между каждой функцией области действия:
 * * То, как они ссылаются на контекстный объект.
 * * Их возвращаемая стоимость.
 * <p>
 * Возвращаемое значение:
 * Функции области действия различаются по возвращаемому ими результату:
 * * apply и also вернуть объект контекста.
 * * let, run, и with возвращаем результат лямбда.
 * <p>
 * Лямбда-результат:
 * команды let, run и with возвращают лямбда-результат. Таким образом, вы можете использовать их при присвоении результата переменной,
 * связывании операций с результатом и так далее.
 */
fun main() {
    //let:
    //Context object: Accessed as it.
    //Returns: The lambda result12.
    //Use case: Useful for null checks and transformations1. It executes only if the object is non-null1. Also, it can introduce an expression as a variable in local scope2.
    // let - часто используется для выполнения блока кода, содержащего ненулевые значения. Чтобы выполнить действия над ненулевым объектом,
    // используйте на нем оператор безопасного вызова ?.let и вызовите с действиями в его лямбде.
    val str = "Hello"
    val length = str?.let {
        println("String is not null: $it")
        it.length // Returns the length of the string
    }
    println("Length: $length") // Length: 5

    // this
    str.run {
        println("The string's length: $length")
        //println("The string's length: ${this.length}") // does the same
    }

    // it
    str?.let {
        println("The string's length is ${it.length}")
    }

    val numbersLet = mutableListOf("one", "two", "three", "four", "five")
    numbersLet.map { it.length }.filter { it > 3 }.let {
        println(it)
        // and more function calls if needed
    }

    //Вы также можете использовать let для введения локальных переменных с ограниченной областью действия, чтобы сделать ваш код более удобным для
    // чтения. Чтобы определить новую переменную для объекта контекста, укажите ее имя в качестве аргумента лямбда, чтобы ее можно было использовать
    // вместо значения по умолчанию it.
    val numbers2 = listOf("one", "two", "three", "four")
    val modifiedFirstItem = numbers2.first().let { firstItem ->
        println("The first item of the list is '$firstItem'")
        if (firstItem.length >= 5) firstItem else "!$firstItem!"
    }.uppercase()
    println("First item after modifications: '$modifiedFirstItem'")


    //run:
    //Context object: Accessed as this. (The context object is available as a receiver (this).)
    //Returns: The lambda result. (The return value is the lambda result.)
    //Use case: Ideal for object initialization and calculations. It can also run statements where an expression is required.
    //runделает то же самое, что withи , но реализовано как функция расширения. Так же как let, вы можете вызвать его на объекте контекста, используя точечную нотацию.
    //
    //runполезно, когда ваша лямбда-функция и инициализирует объекты, и вычисляет возвращаемое значение.
    val service = UserService().run {
        apiUrl = "example.com/api"
        connectTimeout = 5000
        init() //Initialization logic inside the object
        this // Returns the modified object, but the last line of the lambda determines the return value
    }
    val numbersRunEx = mutableListOf("one", "two", "three")
    val countEndsWithE = numbersRunEx.run {
        add("four")
        add("five")
        count { it.endsWith("e") }
    }
    println("There are $countEndsWithE elements that end with e.")

    val service2 = MultiportService("https://example.kotlinlang.org", 80)

    val result = service2.run {
        port = 8080
        query(prepareRequest() + " to port $port")
    }

    // the same code written with let() function:
    val letResult = service2.let {
        it.port = 8080
        it.query(it.prepareRequest() + " to port ${it.port}")
    }

    //Вы также можете вызвать run как функцию без расширения. Вариант без расширения run не имеет контекстного объекта, но он все равно возвращает
    // лямбда-результат. Без расширения run позволяет выполнить блок из нескольких операторов, где требуется выражение. В коде без расширения run
    // можно прочитать как «запустить блок кода и вычислить результат».
    val hexNumberRegex = run {
        val digits = "0-9"
        val hexDigits = "A-Fa-f"
        val sign = "+-"

        Regex("[$sign]?[$digits$hexDigits]+")
    }

    for (match in hexNumberRegex.findAll("+123 -FFFF !%*& 88 XYZ")) {
        println(match.value)
    }


    //with:
    //Context object: Accessed as this. The context object is available as a receiver (this).
    //Returns: The lambda result. (The return value is the lambda result)
    //Use case: Useful for grouping multiple actions on an object.
    val address = Address()
    with(address) {
        street = "123 Main St"
        city = "Anytown"
        zipCode = "12345"
    }

    val numbersWith = mutableListOf("one", "two", "three")
    with(numbersWith) {
        val firstItem = first()
        val lastItem = last()
        println("First item: $firstItem, last item: $lastItem")
    }

    val firstAndLast = with(numbersWith) {
        println("'with' is called with argument $this")
        println("It contains $size elements")
        "The first element is ${first()}," +
                " the last element is ${last()}"
    }
    println(firstAndLast)


    //apply:
    //Context object: Accessed as this. (The context object is available as a receiver (this).)
    //Returns: The object itself. (The return value is the object itself.)
    //Use case: Commonly used for object configuration and initialization.
    //Поскольку apply возвращает сам объект контекста, мы рекомендуем использовать его для блоков кода, которые не возвращают значение и в
    // основном работают с членами объекта-получателя. Наиболее распространенный вариант использования apply — для конфигурации объекта.
    // Такие вызовы можно читать как «применить следующие назначения к объекту».
    //Другим вариантом использования apply является включение apply в несколько цепочек вызовов для более сложной обработки.
    val person = Person().apply {
        name = "John Doe"
        age = 30
        city = "New York"
    }
    println(person) // Person(name=John Doe, age=30, city=New York)


    //also:
    //Context object: Accessed as it. (The context object is available as an argument (it).)
    //Returns: The object itself. (The return value is the object itself.)
    //Use case: Best suited for performing additional side effects, like logging or debugging.
    //also полезно для выполнения некоторых действий, которые принимают объект контекста как аргумент. Используйте also для действий, которым
    // нужна ссылка на объект, а не на его свойства и функции, или когда вы не хотите затенять ссылку this из внешней области.
    //Когда вы видите это also в коде, вы можете прочитать это как «а также сделать следующее с объектом».
    val numbers = mutableListOf("one", "two", "three")
    numbers.also {
        println("The list elements are: $it")
    }.add("four")
    println(numbers)

    val numberList = mutableListOf<Double>()
    numberList.also {
        println("Populating the list")
    }
        .apply {
            add(2.71)
            add(3.14)
            add(1.0)
        }
        .also { println("Sorting the list") }
        .sort()

    println("The list elements are: $numberList")


    //takeIf и takeUnless
    // Помимо функций области действия стандартная библиотека содержит функции takeIf и takeUnless. Эти функции позволяют встраивать проверки
    // состояния объекта в цепочки вызовов.
    // При вызове объекта вместе с предикатом takeIf возвращает этот объект, если он удовлетворяет заданному предикату.
    // В противном случае возвращает null. Таким образом, takeIf является функцией фильтрации для одного объекта.
    //takeUnless - имеет противоположную логику takeIf. При вызове объекта вместе с предикатом takeUnless возвращает null, если он
    // удовлетворяет заданному предикату. В противном случае возвращает объект.
    //При использовании takeIf или takeUnless объект доступен как лямбда-аргумент (it).
    val number = Random.nextInt(100)

    val evenOrNull = number.takeIf { it % 2 == 0 }
    val oddOrNull = number.takeUnless { it % 2 == 0 }
    println("even: $evenOrNull, odd: $oddOrNull")

    val str3 = "Hello"
    val caps = str3.takeIf { it.isNotEmpty() }?.uppercase()
    //val caps = str3.takeIf { it.isNotEmpty() }.uppercase() //compilation error
    println(caps)

    //takeIf и takeUnless особенно полезны в сочетании с функциями области видимости. Например, вы можете объединить takeIf и takeUnless с let для
    // запуска блока кода на объектах, соответствующих заданному предикату. Для этого вызовите takeIf объект, а затем вызовите let с помощью
    // безопасного вызова (?). Для объектов, не соответствующих предикату, takeIf возвращает null и let не вызывается.
    displaySubstringPosition("010000011", "11")
    displaySubstringPosition("010000011", "12")
    displaySubstringPosition2("010000011", "11")
    displaySubstringPosition2("010000011", "12")
}

data class UserService(
    var apiUrl: String? = null,
    var connectTimeout: Int? = null,
) {
    fun init() {}
}

data class Address(
    var street: String? = null,
    var city: String? = null,
    var zipCode: String? = null,
)

data class Person(
    var name: String? = null,
    var age: Int? = null,
    var city: String? = null,
)

data class MultiportService(
    val url: String,
    var port: Int
) {


    fun prepareRequest(): String {
        return "http://$url:$port"
    }

    fun query(query: String): String {
        return "http://$url/$query"
    }
}

fun displaySubstringPosition(input: String, sub: String) {
    input.indexOf(sub).takeIf { it >= 0 }?.let {
        println("The substring $sub is found in $input.")
        println("Its start position is $it.")
    }
}

//For comparison, below is an example of how the same function can be written without using takeIf or scope functions:
fun displaySubstringPosition2(input: String, sub: String) {
    val index = input.indexOf(sub)
    if (index >= 0) {
        println("The substring $sub is found in $input.")
        println("Its start position is $index.")
    }
}