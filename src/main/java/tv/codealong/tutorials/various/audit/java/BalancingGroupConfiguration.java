package tv.codealong.tutorials.various.audit.java;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Описывает настройки балансировки группы провайдеров.
 */
@Data
public class BalancingGroupConfiguration {
    public static final int DEFAULT_WEIGHT = 1;
    /**
     * Название группы балансировки
     */
    private String name;
    /**
     * Признак включения группы балансировки
     */
    private boolean enabled = true;
    /**
     * Максимальный размер сообщения в байтах, обрабатываемого группой балансировки
     */
    private Integer maxMsgSizeBytes;
    /**
     * Признак главной группы балансировки
     */
    private boolean main;
    /**
     * Класс SPI провайдера.
     * Класс провайдера группы балансировки. TODO отказаться от класса, в пользу имени сендера,
     * SPI оставить можно толькок для AUDM.
     */
    private String providerClass;
    /**
     * Режим балансировки для группы
     */
    private BalanceMode balanceMode = BalanceMode.RR;
    /**
     * Информации о формате данных
     */
    private String formatInfo;
    /**
     * Приоритет группы балансировки
     */
    private int priority = 10;
    /**
     * Имена провайдеров, входящих в эту группу, вместе с весом при балансировке.
     */
    private final Map<String, Integer> providerNameToWeight = new HashMap<>();
    /**
     * Конфигурация прерывателя
     */
    private CircuitBreakerConfiguration breakerConfiguration = new CircuitBreakerConfiguration();

    public Map<String, Integer> getProviderNameToWeight() {
        return providerNameToWeight;
    }

    public void addProvider(String name, int weight) {
        providerNameToWeight.merge(
                name,
                weight,
                (oldWeight, newWeight) -> (oldWeight == DEFAULT_WEIGHT) ? newWeight : oldWeight);
    }
}
