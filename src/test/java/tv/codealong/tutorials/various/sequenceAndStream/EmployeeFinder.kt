package tv.codealong.tutorials.various.sequenceAndStream


class EmployeeFinder {
    /**
     * Первый вариант функции, не оптимально написанный
     */
    fun findKotlinDev(departments: List<Department>): Employee {
        return departments
            .map { it.employees }
            .flatten()
            .first { it.position == "KOTLIN_DEVELOPER" }
    }


    fun findKotlinDev2(departments: List<Department>): Employee =
        departments
            .asSequence() // полезно для больших коллекций, чтобы избежать создания промежуточных списков
            .flatMap { it.employees } //flatMap вместо `map + flatten` - это основное улучшение, делает код чище
            .first { it.position == "KOTLIN_DEVELOPER" }


    fun findKotlinDevSafe(departments: List<Department>): Employee? =
        departments
            .asSequence() // полезно для больших коллекций, чтобы избежать создания промежуточных списков
            .flatMap { it.employees } //flatMap вместо `map + flatten` - это основное улучшение, делает код чище
            .firstOrNull { it.position == "KOTLIN_DEVELOPER" } //firstOrNull с обработкой null - безопаснее, чем first который бросает исключение


    fun findKotlinDevSafe2(departments: List<Department>): Employee = departments
        .asSequence() // полезно для больших коллекций, чтобы избежать создания промежуточных списков
        .flatMap { it.employees }
        .firstOrNull { it.position == "KOTLIN_DEVELOPER" } //firstOrNull с обработкой null - безопаснее, чем first который бросает исключение
        ?: throw NoSuchElementException("No KOTLIN_DEVELOPER found in ${departments.size} departments")

    fun findListKotlinDevSafe(departments: List<Department>): List<Employee> = departments
        .asSequence() // полезно для больших коллекций, чтобы избежать создания промежуточных списков
        .flatMap { it.employees.filter { employee -> employee.name == "KOTLIN_DEVELOPER" } }.toList()


    fun findListKotlinDevSafe2(departments: List<Department>): List<Employee> = departments
        .asSequence() // полезно для больших коллекций, чтобы избежать создания промежуточных списков
        .flatMap { it.employees.filter { employee -> employee.name == "KOTLIN_DEVELOPER" } }.toList()
        .let {
            it.ifEmpty { throw NoSuchElementException("No KOTLIN_DEVELOPER found in ${departments.size} departments") }
        }
}
