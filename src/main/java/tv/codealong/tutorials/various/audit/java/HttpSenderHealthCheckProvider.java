package tv.codealong.tutorials.various.audit.java;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLSocketFactory;

public class HttpSenderHealthCheckProvider implements HealthCheckProvider {
    public HttpSenderHealthCheckProvider
            (@NotNull String baseUrl,
             @Nullable SSLSocketFactory sslSocketFactory) {

    }

    @Override
    public HealthState health() {
        return null;
    }
}
