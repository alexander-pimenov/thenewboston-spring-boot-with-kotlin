package tv.codealong.tutorials.various.audit

import org.slf4j.LoggerFactory
import tv.codealong.tutorials.various.audit.java.BalancingGroupConfiguration

class BalancingGroupChecker {
    private val log = LoggerFactory.getLogger(BalancingGroupChecker::class.java)

    fun checkBalancingGroupConfigs(configs: List<BalancingGroupConfiguration>) {
        // Проверка #1: Отсутствует объект с именем "KafkaBuffer group"?
        if (!configs.any { it.name == "KafkaBuffer group" }) {
            log.warn(
                "Отсутствуют объекты с названием 'KafkaBuffer group'. Вы не сможете воспользоваться механизмом гарантированной " +
                        "доставки сообщений в аудит."
            )
        }

        // Проверка #2: Все ли объекты в списке имеют имя "KafkaBuffer group"?
        if (configs.all { it.name == "KafkaBuffer group" }) {
            throw IllegalStateException("Все объекты в списке принадлежат типу 'KafkaBuffer group'. Нужно настроить еще другой транспорт.")
        }
    }
}