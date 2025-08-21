package tv.codealong.tutorials.various.audit.java;

import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Описывает настройки балансировки группы провайдеров.
 */

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

    public BalancingGroupConfiguration() {
    }

    public void addProvider(String name, int weight) {
        providerNameToWeight.merge(
                name,
                weight,
                (oldWeight, newWeight) -> (oldWeight == DEFAULT_WEIGHT) ? newWeight : oldWeight);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getMaxMsgSizeBytes() {
        return maxMsgSizeBytes;
    }

    public void setMaxMsgSizeBytes(Integer maxMsgSizeBytes) {
        this.maxMsgSizeBytes = maxMsgSizeBytes;
    }

    public boolean isMain() {
        return main;
    }

    public void setMain(boolean main) {
        this.main = main;
    }

    public String getProviderClass() {
        return providerClass;
    }

    public void setProviderClass(String providerClass) {
        this.providerClass = providerClass;
    }

    public BalanceMode getBalanceMode() {
        return balanceMode;
    }

    public void setBalanceMode(BalanceMode balanceMode) {
        this.balanceMode = balanceMode;
    }

    public String getFormatInfo() {
        return formatInfo;
    }

    public void setFormatInfo(String formatInfo) {
        this.formatInfo = formatInfo;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Map<String, Integer> getProviderNameToWeight() {
        return providerNameToWeight;
    }

    public CircuitBreakerConfiguration getBreakerConfiguration() {
        return breakerConfiguration;
    }

    public void setBreakerConfiguration(CircuitBreakerConfiguration breakerConfiguration) {
        this.breakerConfiguration = breakerConfiguration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BalancingGroupConfiguration that = (BalancingGroupConfiguration) o;
        return enabled == that.enabled && main == that.main && priority == that.priority && Objects.equals(name, that.name) && Objects.equals(maxMsgSizeBytes, that.maxMsgSizeBytes) && Objects.equals(providerClass, that.providerClass) && balanceMode == that.balanceMode && Objects.equals(formatInfo, that.formatInfo) && Objects.equals(providerNameToWeight, that.providerNameToWeight) && Objects.equals(breakerConfiguration, that.breakerConfiguration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, enabled, maxMsgSizeBytes, main, providerClass, balanceMode, formatInfo, priority, providerNameToWeight, breakerConfiguration);
    }
}
