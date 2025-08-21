`flatMap` - это действительно мощная и элегантная замена комбинации `map + flatten`. 
Давайте разберем как это работает.

## Как работает `map + flatten`:

```kotlin
val departments: List<Department> = // список отделов

// 1. map создает List<List<Employee>>
val listOfEmployeeLists: List<List<Employee>> = departments.map { it.employees }

// 2. flatten преобразует List<List<Employee>> в List<Employee>
val allEmployees: List<Employee> = listOfEmployeeLists.flatten()
```

**Проблема:** создается промежуточная коллекция `List<List<Employee>>`, которая потом преобразуется.

## Как работает `flatMap`:

```kotlin
val allEmployees: List<Employee> = departments.flatMap { it.employees }
```

`flatMap` делает то же самое, но без создания промежуточной коллекции!

## Что происходит внутри flatMap:

1. **Принимает лямбду**, которая преобразует каждый элемент в коллекцию
2. **Автоматически "выравнивает"** все эти коллекции в одну плоскую коллекцию
3. **Возвращает** единый `List<Employee>`

## Визуализация:

Допустим есть:
```kotlin
val dept1 = Department("Backend", listOf(Employee("Иван"), Employee("Петр")))
val dept2 = Department("Frontend", listOf(Employee("Мария")))
val departments = listOf(dept1, dept2)
```

**С map + flatten:**
```
departments → map → [ [Иван, Петр], [Мария] ] → flatten → [Иван, Петр, Мария]
```

**С flatMap:**
```
departments → flatMap → [Иван, Петр, Мария]
```

## Более сложный пример с преобразованием:

`flatMap` особенно полезен, когда нужно не просто извлечь, но и преобразовать данные:

```kotlin
// Получить имена всех сотрудников из всех отделов
val allNames = departments.flatMap { department -> 
    department.employees.map { it.name } 
}
// Результат: ["Иван", "Петр", "Мария"]
```

## Производительность:

- **`flatMap`** обычно быстрее, так как избегает создания промежуточной коллекции
- **`map + flatten`** создает лишний список, что требует дополнительной памяти и времени

## Когда использовать flatMap:

✅ **Всегда**, когда нужно преобразовать коллекцию коллекций в плоскую коллекцию  
✅ Когда нужно извлечь вложенные данные  
✅ Когда нужно применить преобразование и выровнять результат

## Итог:

`flatMap` - это идиоматичный Kotlin способ сделать код:
- **Короче** (1 вызов вместо 2)
- **Читаемее** (понятнее намерение)
- **Эффективнее** (меньше промежуточных коллекций)

Вот почему в Kotlin предпочитают `flatMap` over `map + flatten`! 🚀