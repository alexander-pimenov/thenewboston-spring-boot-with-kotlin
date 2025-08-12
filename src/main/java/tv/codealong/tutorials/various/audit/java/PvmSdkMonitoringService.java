package tv.codealong.tutorials.various.audit.java;

public interface PvmSdkMonitoringService {
    void messageLost();

    void quotaWarnExceed();

    void quotaErrorExceed();

    void lowCriticallyEventLost();

    void lowCriticallyEventSendPoolUsage70();

    void lowCriticallyEventSendPoolUsage100();

    void lowCriticallyEventSendPoolUsage130();

    void quotaTsp(long var1);

    void quotaVsp(double var1);

    void messageOut();

    void messageOutTime();

    void messageSize();

    default void metric(String metricName, String... tagValues) {
        this.metric(metricName, 1.0, tagValues);
    }

    default void metric(PvmSdkMetric metric, String... tagValues) {
        this.metric(metric, 1.0, tagValues);
    }

    void metric(String var1, double var2, String... var4);

    void metric(PvmSdkMetric var1, double var2, String... var4);
}
