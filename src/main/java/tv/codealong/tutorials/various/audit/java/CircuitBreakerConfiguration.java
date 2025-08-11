package tv.codealong.tutorials.various.audit.java;

import lombok.Data;

@Data
public class CircuitBreakerConfiguration {
    private long recoveryTimeout;
    private CircuitBreakerMode mode = CircuitBreakerMode.ERROR_WINDOW;
    private int errorCount = 1;
    private long errorPeriod = 600_000L;
    private int successCount = 1;
    private int failureRate;
    private int executionThreshold;
    private long failureTime;
}
