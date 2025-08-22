package tv.codealong.tutorials.various.other

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

class EmployeeFinderTest {


    @Test
    fun `findKotlinDev should return Kotlin developer when exists in single department`() {
        // Arrange

        val kotlinDev = Employee("Иван Иванов", POSITION_KOTLIN)
        val javaDev = Employee("Петр Петров", POSITION_JAVA)
        val department = Department("Разработка", listOf(javaDev, kotlinDev))
        val departments = listOf(department)

        // Act
        val result = employeeFinder.findKotlinDev(departments)

        // Assert
        assertEquals(POSITION_KOTLIN, result.position)
        assertEquals("Иван Иванов", result.name)
    }

    @Test
    fun `findKotlinDev should return first Kotlin developer when multiple exist`() {
        // Arrange
        val kotlinDev1 = Employee("Иван Иванов", POSITION_KOTLIN)
        val kotlinDev2 = Employee("Мария Сидорова", POSITION_KOTLIN)
        val javaDev = Employee("Петр Петров", "JAVA_DEVELOPER")

        val department1 = Department("Бэкенд", listOf(javaDev, kotlinDev1))
        val department2 = Department("Мобильная разработка", listOf(kotlinDev2))
        val departments = listOf(department1, department2)

        // Act
        val result = employeeFinder.findKotlinDev(departments)

        // Assert
        assertEquals(POSITION_KOTLIN, result.position)
        assertEquals("Иван Иванов", result.name) // Первый найденный
    }

    @Test
    fun `findKotlinDev should return Kotlin developer from multiple departments`() {
        // Arrange
        val frontendDev = Employee("Анна Смирнова", "FRONTEND_DEVELOPER")
        val kotlinDev = Employee("Сергей Кузнецов", POSITION_KOTLIN)
        val qa = Employee("Ольга Новикова", "QA_ENGINEER")

        val frontendDept = Department("Фронтенд", listOf(frontendDev))
        val backendDept = Department("Бэкенд", listOf(kotlinDev))
        val qaDept = Department("Тестирование", listOf(qa))

        val departments = listOf(frontendDept, backendDept, qaDept)

        // Act
        val result = employeeFinder.findKotlinDev(departments)

        // Assert
        assertEquals("KOTLIN_DEVELOPER", result.position)
        assertEquals("Сергей Кузнецов", result.name)
    }

    @Test
    fun `findKotlinDev should throw exception when no Kotlin developers found`() {
        // Arrange
        val javaDev = Employee("Петр Петров", "JAVA_DEVELOPER")
        val pythonDev = Employee("Алексей Алексеев", "PYTHON_DEVELOPER")
        val department = Department("Разработка", listOf(javaDev, pythonDev))
        val departments = listOf(department)

        assertThrows(NoSuchElementException::class.java) {
            // Act & Assert
            employeeFinder.findKotlinDev(departments)
        }

    }

    @Test
    fun `findListKotlinDev should return empty list`() {
        // Arrange
        val javaDev = Employee("Петр Петров", "JAVA_DEVELOPER")
        val pythonDev = Employee("Алексей Алексеев", "PYTHON_DEVELOPER")
        val department = Department("Разработка", listOf(javaDev, pythonDev))
        val departments = listOf(department)


        // Act & Assert
        val employeeList = employeeFinder.findListKotlinDevSafe(departments)

        assertTrue(employeeList.isEmpty())
    }

    @Test
    fun `findKotlinDev should handle empty departments list`() {
        // Arrange
        val departments = emptyList<Department>()

        // Act & Assert
        try {
            employeeFinder.findKotlinDev(departments)
            fail("Expected NoSuchElementException")
        } catch (e: NoSuchElementException) {
            // Expected behavior
        }
    }

    @Test
    fun `findKotlinDev should handle departments with empty employees list`() {
        // Arrange
        val emptyDept = Department("Пустой отдел", emptyList())
        val kotlinDev = Employee("Иван Иванов", POSITION_KOTLIN)
        val nonEmptyDept = Department("Разработка", listOf(kotlinDev))

        val departments = listOf(emptyDept, nonEmptyDept)

        // Act
        val result = employeeFinder.findKotlinDev(departments)

        // Assert
        assertEquals("KOTLIN_DEVELOPER", result.position)
        assertEquals("Иван Иванов", result.name)
    }

    @Test
    fun `findKotlinDev should be case sensitive for position`() {
        // Arrange
        val wrongCaseDev = Employee("Иван Иванов", "kotlin_developer") // lowercase
        val correctDev = Employee("Петр Петров", POSITION_KOTLIN) // correct case
        val department = Department("Разработка", listOf(wrongCaseDev, correctDev))
        val departments = listOf(department)

        // Act
        val result = employeeFinder.findKotlinDev(departments)

        // Assert
        assertEquals("KOTLIN_DEVELOPER", result.position)
        assertEquals("Петр Петров", result.name)
    }

    @Test
    fun `findKotlinDevSafe should return null when no Kotlin developers found`() {
        // Arrange
        val javaDev = Employee("Петр Петров", "JAVA_DEVELOPER")
        val department = Department("Разработка", listOf(javaDev))
        val departments = listOf(department)

        // Act
        val result = employeeFinder.findKotlinDevSafe(departments)

        // Assert
        assertNull(result)
    }

    @Test
    fun `findKotlinDevSafe should return null when no Kotlin developers found with exception`() {
        // Arrange
        val javaDev = Employee("Петр Петров", "JAVA_DEVELOPER")
        val department = Department("Разработка", listOf(javaDev))
        val departments = listOf(department)

        val errorRes = assertThrows(NoSuchElementException::class.java) {
            // Act
            employeeFinder.findKotlinDevSafe2(departments)
        }

        assertEquals("No KOTLIN_DEVELOPER found in 1 departments", errorRes.message)
    }

    @Test
    fun `findKotlinDev when using sequence`() {
        // Arrange
        val dept1 = Department("Backend", listOf(
            Employee("Иван", "JAVA_DEVELOPER"),
            Employee("Петр", "KOTLIN_DEVELOPER"), // ← здесь наш разработчик!
            Employee("Bob", "CPP_DEVELOPER")
        ))

        val dept2 = Department("Frontend", listOf(
            Employee("Мария", "JS_DEVELOPER"),
            Employee("Анна", "KOTLIN_DEVELOPER"),
            Employee("Jack", "PYTHON_DEVELOPER")
        ))

        val departments = listOf(dept1, dept2)

        // Act
        val result = employeeFinder.findKotlinDevSafe2(departments)

        assertEquals("Петр", result.name)
    }

    @Test
    fun `test for measuring sequence processing (benchmarck)`() {
        val largeList = (1..1_000_000).toList()

        // Медленнее и использует больше памяти
        val time1 = measureTimeMillis {
            largeList.map { it * 2 }.filter { it % 3 == 0 }.take(10).toList()
        }

        // Быстрее и эффективнее
        val time2 = measureTimeMillis {
            largeList.asSequence().map { it * 2 }.filter { it % 3 == 0 }.take(10).toList()
        }

        println("List: $time1 ms, Sequence: $time2 ms") //List: 32 ms, Sequence: 6 ms
    }

    companion object {

        const val POSITION_KOTLIN = "KOTLIN_DEVELOPER"
        const val POSITION_JAVA = "KOTLIN_JAVA"
        private lateinit var employeeFinder: EmployeeFinder

        @JvmStatic
        @BeforeAll
        fun setUp(): Unit {
            employeeFinder = EmployeeFinder()
        }
    }
}

data class Employee(
    val name: String,
    val position: String,
    val department: String = ""
)

data class Department(
    val name: String,
    val employees: List<Employee>
)
