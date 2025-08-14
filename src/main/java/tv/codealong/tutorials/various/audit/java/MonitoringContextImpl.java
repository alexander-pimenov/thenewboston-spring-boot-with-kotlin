package tv.codealong.tutorials.various.audit.java;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ParametersAreNonnullByDefault
public class MonitoringContextImpl implements MonitoringContext {
    private final Map<String, Map<String, String>> tagsByComponents = new ConcurrentHashMap<>();

    public MonitoringContextImpl() {
    }

    @Override
    public @Nullable Map<String, String> getTagValues(String componentName) {
        return (Map) this.tagsByComponents.get(componentName);
    }

    @Override
    public void setTagValues(String componentName, @Nullable Map<String, String> tagValues) {
        if (tagValues == null) {
            this.tagsByComponents.remove(componentName);
        } else {
            ((Map) this.tagsByComponents.computeIfAbsent(componentName, (k) -> {
                return new HashMap();
            })).putAll(tagValues);
        }
    }
}
