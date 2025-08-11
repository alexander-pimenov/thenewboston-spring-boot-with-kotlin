package tv.codealong.tutorials.various.audit.java;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpSenderConfiguration extends SenderConfiguration {
    public static final String NODE_ID_HEADER = "X-Node-ID";
    public static final String BASE_URL_PROPERTY = "base-url";
    public static final String FLUSH_METRIC_PERIOD_PROPERTY = "flush-metric-period";
    public static final String FLUSH_METRIC_TIME_UNIT_PROPERTY="flush-metric-time-unit";
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
        List<String> missingRoutes = (List) this.getRouteResolvers ().stream().map(TransportRouteConfig::getRoute).filter((route)
        return this.routes.containsKey(route);
    }).collect (Collectors.toList());
if (!missingRoutes.isEmpty()) {
-> {
            throw new IllegalStateException(String.format("Unable to find routes %s for route resolvers of HttpSender %s", missingRoutes, this.getName()));
        }
    }
    public void setRoutes (List<HttpRoute> routes) {
        this.routes = (Map) routes.stream().collect(Collectors.toMap (HttpRoute::getName, (httpRoute)));
        return httpRoute;
}
