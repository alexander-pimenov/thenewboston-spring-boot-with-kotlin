package tv.codealong.tutorials.various.audit.java;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HttpSenderConfiguration extends SenderConfiguration {
    public static final String NODE_ID_HEADER = "X-Node-ID";
    public static final String BASE_URL_PROPERTY = "base-url";
    public static final String FLUSH_METRIC_PERIOD_PROPERTY = "flush-metric-period";
    public static final String FLUSH_METRIC_TIME_UNIT_PROPERTY = "flush-metric-time-unit";
    public static final String RETRY_PERIOD_MS_PROPERTY = "retry-period-ms";
    public static final String RETRY_MAX_PERIOD_MS_PROPERTY = "retry-max-period-ms";
    public static final String RETRY_MAX_ATTEMPTS_PROPERTY = "retry-max-attempts";
    public static final String MAX_TOTAL_HTTP_CONNECTIONS = "max-total-http-connections";
    public static final String DEFAULT_MAX_PER_ROUTE = "default-max-per-route";
    private Map<String, Object> properties;
    private Map<String, HttpRoute> routes;

    public void validate() {
        this.checkRequiredProperty("base-url");
        this.checkRequiredBaseHeader("X-Node-ID");
        List<String> missingRoutes = (List) this.getRouteResolvers().stream().map(TransportRouteConfig::getRoute).filter((route) -> {
            return !this.routes.containsKey(route);
        }).collect(Collectors.toList());
        if (!missingRoutes.isEmpty()) {
            throw new IllegalStateException(String.format("Unable to find routes %s for route resolvers of HttpSender %s", missingRoutes, this.getName()));
        }
    }

    private void checkRequiredBaseHeader(String header) {
        if (!this.properties.containsKey(header)) {
            throw new IllegalStateException(String.format("Unable to find required base header %s for HttpSender %s", header, this.getName()));
        }
    }

    private void checkRequiredProperty(String property) {
        if (!this.properties.containsKey(property)) {
            throw new IllegalStateException(String.format("Unable to find required property %s for HttpSender %s", property, this.getName()));
        }
    }

    public void setRoutes(List<HttpRoute> routes) {
        this.routes = (Map) routes.stream().collect(Collectors.toMap(HttpRoute::getName, (httpRoute) -> {
            return httpRoute;
        }));
    }

    private long getLongOrDefault(String key, long defaultValue) {
        return (Long) Optional.ofNullable(this.properties.get(key)).map((value) -> {
            return value instanceof Integer ? ((Integer) value).longValue() : (Long) value;
        }).orElse(defaultValue);
    }

    private int getIntOrDefault(String key, int defaultValue) {
        return (Integer) Optional.ofNullable(this.properties.get(key)).map((value) -> {
            return (Integer) value;
        }).orElse(defaultValue);
    }


    public @NotNull String getBaseUrl() {
        return (String) this.properties.get(BASE_URL_PROPERTY);
    }

    public long getFlushMetricPeriod() {
        return this.getLongOrDefault(FLUSH_METRIC_PERIOD_PROPERTY, 0L);
    }

    public @Nullable TimeUnit getFLushMetricTimeUnit() {
        return (TimeUnit) Optional.ofNullable(this.properties.get(FLUSH_METRIC_TIME_UNIT_PROPERTY)).map((value) -> {
            return (String) value;
        }).map(TimeUnit::valueOf).orElse((TimeUnit) null);
    }

    public long getRetryPeriodMs() {
        return this.getLongOrDefault(RETRY_PERIOD_MS_PROPERTY, 0L);
    }

    public long getRetryMaxPeriodMs() {
        return this.getLongOrDefault(RETRY_MAX_PERIOD_MS_PROPERTY, 0L);
    }

    public int getRetryMaxAttempts() {
        return this.getIntOrDefault(RETRY_MAX_ATTEMPTS_PROPERTY, 0);
    }

    public int getDefaultMaxPerRoute() {
        return this.getIntOrDefault(DEFAULT_MAX_PER_ROUTE, 0);
    }

    public int getTotalHttpConnections() {
        return this.getIntOrDefault(MAX_TOTAL_HTTP_CONNECTIONS, 0);
    }

    public Map<String, HttpRoute> getRoutes(String name) {
        return this.routes;
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, HttpRoute> getRoutes() {
        return this.routes;
    }

    public void setRoutes(Map<String, HttpRoute> routes) {
        this.routes = routes;
    }

    public HttpSenderConfiguration() {
    }
}
