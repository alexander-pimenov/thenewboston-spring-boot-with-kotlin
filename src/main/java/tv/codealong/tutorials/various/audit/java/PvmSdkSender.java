package tv.codealong.tutorials.various.audit.java;

import org.jetbrains.annotations.NotNull;

public interface PvmSdkSender extends AutoCloseable {
    @NotNull
    String name();

    @NotNull
    HealthState healthCheck();

    void send(@NotNull PvmTransportMessage var1);

    default void close() throws Exception {
        throw new UnsupportedOperationException("Feature incomplete. Contact assistance.");
    }

    default boolean isBuffer() {
        return false;
    }
}
