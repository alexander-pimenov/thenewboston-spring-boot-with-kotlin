package tv.codealong.tutorials.various.audit

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

/**
 * Разберём класс по частям и объясним, почему структура именно такая.
 *
 * ---
 *
 * ### 1. `@ConstructorBinding`
 *
 * Аннотация `@ConstructorBinding` говорит Spring Boot, что бин конфигурации должен быть создан **через конструктор**, а не через сеттеры или изменение свойств после инициализации.
 *
 * По умолчанию Spring Boot для `@ConfigurationProperties` использует механизм **JavaBean-style binding** (через геттеры/сеттеры). Но для `data class` в Kotlin мы обычно не пишем сеттеров, и хотим, чтобы объект был **иммутабельным**.
 * Поэтому:
 *
 * * `@ConstructorBinding` позволяет Spring связать YAML/Properties напрямую с параметрами конструктора.
 * * Это гарантирует, что `AuditProperties` будет создан сразу в корректном состоянии и больше не изменится.
 *
 * > Начиная с Spring Boot 2.2, `@ConstructorBinding` стала стандартным способом биндинга для классов без сеттеров, а в новых версиях (2.6+) её можно даже не писать, если используется `kapt` и `@ConfigurationPropertiesScan`. Но в старых версиях её указывали явно.
 *
 * ---
 *
 * ### 2. Почему во вторичном конструкторе поменяли порядок аргументов и сделали их nullable?
 *
 * Посмотрим на класс:
 *
 * ```kotlin
 * data class AuditProperties(
 *     val enabled: Boolean,
 *     val client: AuditClientProperties
 * )
 * ```
 *
 * Это **первичный конструктор** — он задаёт финальные неизменяемые свойства.
 *
 * Далее идёт **вторичный конструктор**:
 *
 * ```kotlin
 * @ConstructorBinding
 * constructor(client: AuditClientProperties?, enabled: Boolean?)
 * ```
 *
 * Здесь происходит следующее:
 *
 * * **Nullable-параметры** нужны, потому что при биндинге из `application.yml` свойства могут отсутствовать. Например, если `audit.enabled` не задан, Spring должен иметь возможность передать `null`.
 * * Порядок аргументов может быть любым — это просто сигнатура вторичного конструктора, он вручную вызывает первичный:
 *
 *   ```kotlin
 *   this(
 *       enabled = enabled != false,
 *       client = if (enabled != false) {
 *           requireNotNull(client) { "блок audit.client обязателен ..." }
 *       } else {
 *           AuditClientProperties(AuditClientModeEnum.ASYNC, DISABLED_VALUE, DISABLED_VALUE, BalancingGroup.NONE)
 *       }
 *   )
 *   ```
 *
 *   Здесь добавляется логика:
 *
 *   * Если `enabled == false` или `enabled == null` → выключаем аудит и подставляем "заглушку".
 *   * Если `enabled != false` → проверяем, что `client` не `null`. Если он не задан, бросаем исключение.
 *
 * Таким образом:
 *
 * * Вторичный конструктор обрабатывает **опциональные входные данные от Spring**.
 * * Первичный конструктор всегда получает **валидные финальные значения**.
 *
 * ---
 *
 * ✅ **Итоговая причина:**
 *
 * * **`@ConstructorBinding`** нужен, чтобы бин создавался через конструктор.
 * * Вторичный конструктор с nullable-параметрами выполняет валидацию и подстановку значений по умолчанию.
 * * Первичный конструктор содержит уже готовые финальные значения.
 *
 * ---
 *
 */
@ConfigurationProperties(prefix = "audit")
data class AuditProperties(
    val enabled: Boolean,
    val client: AuditClientProperties
) {
    @ConstructorBinding
    constructor(client: AuditClientProperties?, enabled: Boolean?) :
            this(
                enabled = enabled != false,
                client = if (enabled != false) {
                    requireNotNull(client) { "блок audit.client обязателен для заполнения. (nodeId, mode, project)" }
                    client
                } else AuditClientProperties(
                    AuditClientModeEnum.ASYNC,
                    DISABLED_VALUE,
                    DISABLED_VALUE,
                    BalancingGroup.NONE
                )
            )
}

