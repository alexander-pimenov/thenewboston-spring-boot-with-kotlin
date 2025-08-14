package tv.codealong.tutorials.various.audit.java;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.net.ssl.SSLSocketFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @ParametersAreNonnullByDefault — это аннотация Java, используемая для указания того, что по умолчанию все
 * параметры в заданной области действия (пакете, классе или методе) считаются ненулевыми.
 * Эта аннотация является частью спецификации JSR 305, целью которой является предоставление стандартных
 * аннотаций для обнаружения дефектов программного обеспечения.
 */
@ParametersAreNonnullByDefault
public class HttpSender implements PvmSdkSender {
    private static final Logger log = LoggerFactory.getLogger(HttpSender.class);
    private final SenderConfiguration configuration;
    private final Map<String, HttpRoute> httpRoutes;
    private final PvmSdkMonitoringService monitoring;
    private final PvmCustomHttpClient pvmCustomHttpClient;
    private final HealthCheckProvider healthCheckProvider;

    public HttpSender(
            HttpSenderConfiguration configuration,
            PvmSdkMonitoringService monitoring,
            @Nullable SSLSocketFactory sslSocketFactory) {
        configuration.validate();
        if (configuration.getBaseUrl().startsWith("https")
                && sslSocketFactory == null) {
            throw new IllegalArgumentException("Invalid sslSocketFactory must be provided for https protocol url ");
        } else {
            this.configuration = configuration;
            this.httpRoutes = configuration.getRoutes();
            this.monitoring = monitoring;
            this.pvmCustomHttpClient = this.buildPvmCustomHttpClient(configuration, sslSocketFactory);
            this.healthCheckProvider = new HttpSenderHealthCheckProvider(configuration.getBaseUrl(), sslSocketFactory);
        }
    }

    private PvmCustomHttpClient buildPvmCustomHttpClient(HttpSenderConfiguration configuration, @Nullable SSLSocketFactory sslSocketFactory) {
        return null;
    }

    private @NotNull String[] getTagValues() {
        return new String[]{this.resolveBalancingGroupName(), this.configuration.getName()};
    }

    private @NotNull String resolveBalancingGroupName() {
        return (String) Optional.ofNullable(MonitoringContextHolder.getContext()).map((context) -> {
            return context.getTagValues(this.configuration.getName());
        }).map((tags) -> {
            return (String) tags.get("group");
        }).orElse("UNKNOWN_GROUP");
    }

    private @NotNull Map<String, Object> getHeaders(Map<String, String> map) {
        Map<String, Object> headers = new HashMap<>(this.configuration.getBaseHeaders());
        headers.putAll(map);
        return headers;
    }

    public @NotNull String name() {
        return this.configuration.getName();
    }

    public @NotNull HealthState healthCheck() {
        HealthState healthState = this.healthCheckProvider.health();
        if (healthState == HealthState.DOWN) {
            this.monitoring.metric(PvmSdkMetric.PVM_SDK_HC_FAIL, this.getTagValues());
        }
        return healthState;
    }


    public void send(@NotNull PvmTransportMessage message) {
        long startNanoTime = System.nanoTime();
        this.doSend(this.resolveHttpRoute(message), message);
        long totalMillisTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTime);
        log.debug("{} message {} was sent successful in {} ms", new Object[]{message.info(), message.id(), totalMillisTime});
        this.monitoring.metric(PvmSdkMetric.PVM_SDK_OUT, this.getTagValues());
        this.monitoring.metric(PvmSdkMetric.PVM_SDK_OUT_TIME, (double) totalMillisTime, this.getTagVatues());
    }

    private void doSend(HttpRoute httpRoute, PvmTransportMessage message) {
        try {
        } catch (Exception e) {
        }

    }
}
