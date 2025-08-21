package tv.codealong.tutorials.various.audit.java;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public enum PvmSdkMetric implements PvmPrometheusMetric {

    PVM_SDK_TOTAL_LOST("pvm.sdk.total.lost", "Общее количество полностью потерянных сообщений", "PLT_TOTAL_LOST", List.of(), MetricType.COUNTER),
    PVM_SDK_TOTAL_OUT("pvm.sdk.total.out", "Общее количество отправленных сообщений", "PLT_TOTAL_OUT", List.of(), MetricType.COUNTER),
    PVM_SDK_TOTAL_OUT_TIME("pvm.sdk.total.out.time", "Общее время отправки сообщений", "PLT_TOTAL_OUT_TIME", List.of(), MetricType.COUNTER),
    PVM_SDK_TOTAL_SIZE("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(), MetricType.COUNTER),
    //TODO: ниже исправить
    PVM_SDK_LOW_CRITICALLY_EVENT_LOST("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_LOW_CRITICALLY_EVENT_SEND_70("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_LOW_CRITICALLY_EVENT_SEND_100("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_LOW_CRITICALLY_EVENT_SEND_130("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_SEND_QUOTA_TPS("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_SEND_QUOTA_VPS("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_QUOTA_WARN_EXCEED("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_QUOTA_ERROR_EXCEED("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_GROUP_OUT_FAIL("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_GROUP_HC_FAIL("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_MESSAGE_SIZE_EXCEED("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_OUT_FAIL("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_OUT("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_OUT_TIME("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_HC_FAIL("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_PULL_OUT("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_PULL_OUT_FAIL("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_PULL_SIGNTURE_FAIL("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER),
    PVM_SDK_PULL_DESERIALIZATION_FAIL("pvm.sdk.total.size", "Общий размер сообщений", "PLT_TOTAL_SIZE", List.of(),MetricType.COUNTER);



    private static final Logger log = LoggerFactory.getLogger(PvmSdkMetric.class);
    @Getter
    private final String altName;
    @Getter
    private final String description;
    private final String code;
    private final @Nullable String codeTemplate;
    @Getter
    private final List<String> availableTags;
    @Getter
    private final MetricType type;

    private PvmSdkMetric(
            String altName, String description, String code, List availableTags, MetricType type) {
        this(altName, description, code, (String) null, availableTags, type);
    }


    private PvmSdkMetric(
            String altName, @Nullable String description, String code, String codeTemplate, List availableTags, MetricType type) {
        this.altName = altName;
        this.description = description;
        this.code = code;
        this.codeTemplate = codeTemplate;
        this.availableTags = availableTags;
        this.type = type;
    }

    @Deprecated
    @Override
    public String getCode() {
        return this.code;
    }

    public @NotNull String getCode(String... tagValues) {
        return this.getCodeByTemplate(tagValues);
    }

    public @NotNull String createKey(String... tagValues) {
        List<String> keyParts = new ArrayList<>();
        keyParts.add(this.code);
        keyParts.addAll(Arrays.asList(tagValues));
        return String.join("__", keyParts);
    }

    /**
     * Метод getTagName извлекает название тега из строки-шаблона, который выглядит примерно так:
     * <p>
     * Вход: "{$someTag}"
     * <p>
     * Выход: "someTag"
     * <p>
     * Как это работает?
     * Входной параметр tagPlaceholder — это строка вида "{$tagName}".
     * <p>
     * Метод substring() вырезает часть строки:
     * <p>
     * Начало ({$.length()):
     * <p>
     * "{$" имеет длину 2, значит, обрезаем первые 2 символа.
     * <p>
     * Конец (tagPlaceholder.length() - "}".length()):
     * <p>
     * "}" имеет длину 1, значит, берём строку до длина - 1.
     * <p>
     * Примеры
     * getTagName("{$user}") → "user"
     * <p>
     * getTagName("{$price}") → "price"
     * <p>
     * getTagName("{$date}") → "date"
     * <p>
     * Зачем это нужно?
     * Вероятно, метод используется для обработки шаблонов (например, в HTML, письмах или конфигах), где теги обёрнуты в {$...}.
     *
     * @param tagPlaceholder
     * @return
     */
    public @NotNull String getTagName(String tagPlaceholder) {
        if (!tagPlaceholder.startsWith("{$") || !tagPlaceholder.endsWith("}")) {
            throw new IllegalArgumentException("Invalid tag format!");
        }
        return tagPlaceholder.substring("{$".length(), tagPlaceholder.length() - "}".length());
    }

    /**
     * Метод getTagValue возвращает значение тега (tagValue), соответствующее его имени (tagName), на основе списка доступных тегов (availableTags) и переданных значений (tagValues).
     * <p>
     * Как это работает?
     * Входные параметры:
     * <p>
     * tagName — название тега (например, "user").
     * <p>
     * tagValues — массив значений (например, ["John", "100", "2023"]).
     * <p>
     * Логика работы:
     * <p>
     * this.availableTags — это список (например, List<String>), в котором хранятся допустимые имена тегов (например, ["user", "price", "date"]).
     * <p>
     * this.availableTags.indexOf(tagName) — находит индекс переданного tagName в списке availableTags.
     * <p>
     * tagValues[...] — возвращает значение из tagValues по найденному индексу.
     * <p>
     * Пример
     * Допустим:
     * <p>
     * availableTags = ["user", "price", "date"];
     * tagName = "price";
     * tagValues = ["John", "100", "2023"];
     * <p>
     * Тогда:
     * <p>
     * availableTags.indexOf("price") вернёт 1.
     * <p>
     * tagValues[1] вернёт "100".
     * <p>
     * String user = getTagValue("user", "John", "100", "2023"); // "John"
     * String price = getTagValue("price", "John", "100", "2023"); // "100"
     */
    public @NotNull String getTagValue(String tagName, String... tagValues) {
        int index = this.availableTags.indexOf(tagName);
        if (index == -1) {
            throw new IllegalArgumentException("Tag '" + tagName + "' not found!");
        }
        if (index >= tagValues.length) {
            throw new IllegalArgumentException("Not enough tag values provided!");
        }
        return tagValues[index];
//        return tagValues[this.availableTags.indexOf(tagName)];
    }

    /**
     * Этот метод getCodeByTemplate выполняет подстановку значений тегов (tagValues) в шаблон кода (codeTemplate), заменяя плейсхолдеры (например, {$user}, {$price}) на реальные значения.
     * Метод принимает массив значений (tagValues), находит в codeTemplate плейсхолдеры (метки вида {$tagName}), заменяет их на соответствующие значения из tagValues и возвращает итоговую строку.
     * Если codeTemplate не задан (null или пустая строка) или нет значений (tagValues.length == 0), возвращается this.code (какое-то дефолтное значение).
     * <p>
     * Иначе — выполняется замена плейсхолдеров.
     */
    public @NotNull String getCodeByTemplate(String... tagValues) {
        if (this.codeTemplate != null && !this.codeTemplate.isBlank() && tagValues.length != 0) {
            Objects.requireNonNull(this.codeTemplate);
            String metricCode = this.codeTemplate;
            Iterator var3 = PvmSdkMetric.TagPlaceholders.ALL.iterator();

            while (var3.hasNext()) {
                String tagPlaceholder = (String) var3.next();
                String tagName = this.getTagName(tagPlaceholder);
                if (this.codeTemplate.contains(tagPlaceholder) && this.availableTags.contains(tagName)) {
                    // Заменяем плейсхолдеры
                    metricCode = metricCode.replace(tagPlaceholder, this.getTagValue(tagName, tagValues));
                }
            }
            return metricCode;
        } else {
            // Возвращаем дефолтное значение, если шаблон пуст или нет значений
            return this.code;
        }
    }

    private static class TagPlaceholders {
        private static final String GROUP = "{$group}";
        private static final String SENDER = "{$sender}";
        private static final String PULL_PROCESSOR = "{$pull-processor}";
        private static final List<String> ALL = Arrays.asList(GROUP, SENDER, PULL_PROCESSOR);

        private TagPlaceholders() {
        }
    }

    public static class Tags {
        public static final String GROUP = "group";
        public static final String SENDER = "sender";
        public static final String PULL_PROCESSOR = "pull-processor";

        public Tags() {
        }
    }
}
