package tv.codealong.tutorials.various.audit.java;

public class GlobalMonitoringContextHolderStrategy implements MonitoringContextHolderStrategy {
    private static MonitoringContext contextHolder;

    public GlobalMonitoringContextHolderStrategy() {
    }

    @Override
    public void clearContext() {
        contextHolder = null;
    }

    @Override
    public MonitoringContext getContext() {
        if (contextHolder == null) {
            contextHolder = new MonitoringContextImpl();
        }
        return contextHolder;
    }

    @Override
    public void setContext(MonitoringContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Only non-null MonitoringContext instances are permitted");
        } else {
            contextHolder = context;
        }
    }

    @Override
    public MonitoringContext createEmptyContext() {
        return new MonitoringContextImpl();
    }
}
