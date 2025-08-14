package tv.codealong.tutorials.various.audit.java;

public interface MonitoringContextHolderStrategy {
    void clearContext();

    MonitoringContext getContext();

    void setContext(MonitoringContext var1);

    MonitoringContext createEmptyContext();
}
