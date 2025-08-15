package tv.codealong.tutorials.various.audit.java;

import feign.HeaderMap;
import feign.Param;
import feign.RequestLine;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocketFactory;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface PvmCustomHttpClient {
    long DEFAULT_FLUSH_METRIC_PERIOD = 1L;
    TimeUnit DEFAULT_FLUSH_METRIC_TIME_UNIT = TimeUnit.HOURS;
    long DEFAULT_RETRY_PERIOD_MS = 100L;
    long DEFAULT_RETRY_MAX_PERIOD_MS = 1000L;
    int DEFAULT_RETRY_MAX_ATTEMPTS = 3;
    int DEFAULT_MAX_TOTAL_HTTP_CONNECTIONS = 10 + Runtime.getRuntime().availableProcessors() * 20;
    int DEFAULT_DEFAULT_MAX_PER_ROUTE = Runtime.getRuntime().availableProcessors() * 20;

    static PvmCustomHttpClientBuilder builder() {
        return new PvmCustomHttpClientBuilder();
    }

    @RequestLine("POST /{path}")
    Object postByRoute(@Param("path") String var1, @HeaderMap Map<String, Object> var2, Object var3);

    public static class PvmCustomHttpClientBuilder {
        public static final Logger LOGGER = LoggerFactory.getLogger(PvmCustomHttpClient.class);
        private String baseUrl;
        private SSLSocketFactory sslSocketFactory;
        private long flushMetricPeriod = 1L;
        private TimeUnit flushMetricTimeUnit;
        private long retryPeriodMs;
        private long retryMaxPeriodMs;
        private int retryMaxAttempts;
        private int maxTotalHttpConnections;
        private int defaultMaxPerRoute;

        public PvmCustomHttpClientBuilder() {
            this.flushMetricTimeUnit = PvmCustomHttpClient.DEFAULT_FLUSH_METRIC_TIME_UNIT;
            this.retryPeriodMs = PvmCustomHttpClient.DEFAULT_RETRY_PERIOD_MS;
            this.retryMaxPeriodMs = PvmCustomHttpClient.DEFAULT_RETRY_MAX_PERIOD_MS;
            this.retryMaxAttempts = PvmCustomHttpClient.DEFAULT_RETRY_MAX_ATTEMPTS;
            this.maxTotalHttpConnections = PvmCustomHttpClient.DEFAULT_MAX_TOTAL_HTTP_CONNECTIONS;
            this.defaultMaxPerRoute = PvmCustomHttpClient.DEFAULT_DEFAULT_MAX_PER_ROUTE;
        }

        public PvmCustomHttpClientBuilder baseUrl(@NotNull String baseUrl) {
            if (StringUtil.isEmpty(baseUrl)) {
                throw new IllegalArgumentException("Base url must not be null or empty");
            } else {
                this.baseUrl = baseUrl;
                return this;
            }
        }

        public PvmCustomHttpClientBuilder sslSocketFactory(@NotNull SSLSocketFactory sslSocketFactory) {
            this.sslSocketFactory = sslSocketFactory;
            return this;
        }

        public PvmCustomHttpClientBuilder flushMetricPeriod(long period, TimeUnit unit) {
            this.flushMetricPeriod = period;
            this.flushMetricTimeUnit = unit;
            return this;
        }

        public PvmCustomHttpClientBuilder retryPeriod(long periodMs) {
            this.retryPeriodMs = periodMs;
            return this;
        }


        public PvmCustomHttpClientBuilder retryMaxPeriod(long maxPeriodMs) {
            this.retryMaxPeriodMs = maxPeriodMs;
            return this;
        }

        public PvmCustomHttpClientBuilder retryMaxAttempts(int attempts) {
            this.retryMaxAttempts = attempts;
            return this;
        }

        public PvmCustomHttpClientBuilder maxTotalHttpConnections(@NotNull Integer maxTotalHttpConnections) {
            this.maxTotalHttpConnections = maxTotalHttpConnections;
            return this;
        }

        public PvmCustomHttpClientBuilder defaultMaxPerRoute(@NotNull Integer defaultMaxPerRoute) {
            this.defaultMaxPerRoute = defaultMaxPerRoute;
            return this;
        }

        public @NotNull PvmCustomHttpClient build() {
            this.validate();
            return new PvmCustomHttpClientImpl();
        }

        protected void validate() {

        }
    }
}
