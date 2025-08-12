package tv.codealong.tutorials.various.audit.java;


import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.MultiGauge.Row;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface PvmPrometheusMetric {
    String getCode();

    String getDescription();

    List<String> getAvailableTags();

    MetricType getType();

    default List<Tag> getTags(Map<String, String> tags) {
        return (List) tags.entrySet().stream().filter((entry) -> {
            return this.getAvailableTags().contains(entry.getKey());
        }).map((entry) -> {
            return Tag.of((String) entry.getKey(), (String) entry.getValue());
        }).collect(Collectors.toList());
    }

    default MultiGauge.Row<Number> createMultiGaugeRow(Map<String, String> tags, Long value) {
        return Row.of(Tags.of(this.getTags(tags)), value);
    }
}
