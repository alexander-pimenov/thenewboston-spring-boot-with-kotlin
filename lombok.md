Аннотация **`@Data`** из библиотеки **Lombok** автоматически генерирует несколько стандартных методов для класса, чтобы уменьшить объем шаблонного кода.

### **Какие методы создает `@Data`?**
1. **Геттеры** (для всех не-`final` полей)
    - `getFieldName()` для каждого поля (если поле `boolean` и начинается с `is`, то может быть `isFieldName()`).

2. **Сеттеры** (для всех не-`final` и не-`static` полей)
    - `setFieldName(T value)`

3. **`equals()`** – сравнивает объекты по значениям полей (а не по ссылке).

4. **`hashCode()`** – генерирует хеш-код на основе значений полей.

5. **`toString()`** – возвращает строковое представление объекта в формате:
   ```java
   ClassName(field1=value1, field2=value2, ...)
   ```  

6. **`RequiredArgsConstructor`** (конструктор для всех `final` и `@NonNull` полей)
    - Если в классе есть `final`-поля или поля, помеченные `@NonNull`, Lombok создаст конструктор с этими параметрами.

### **Пример с `@Data`**
```java
import lombok.Data;

@Data
public class Person {
    private final String id;  // будет в конструкторе
    private String name;
    private int age;
    private boolean active;
}
```  

### **Эквивалент без Lombok**
```java
public class Person {
    private final String id;
    private String name;
    private int age;
    private boolean active;

    // Конструктор для final-поля
    public Person(String id) {
        this.id = id;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // equals(), hashCode(), toString()
    @Override
    public boolean equals(Object o) { ... }
    @Override
    public int hashCode() { ... }
    @Override
    public String toString() { ... }
}
```  

### **Важные замечания**
- Если нужно кастомизировать поведение (например, исключить поле из `equals`/`hashCode`), можно использовать:
    - `@EqualsAndHashCode.Exclude`
    - `@ToString.Exclude`
- `@Data` включает в себя аннотации:
    - `@Getter`
    - `@Setter`
    - `@ToString`
    - `@EqualsAndHashCode`
    - `@RequiredArgsConstructor`

Если вам не нужны все эти методы, лучше использовать отдельные аннотации Lombok.

---

