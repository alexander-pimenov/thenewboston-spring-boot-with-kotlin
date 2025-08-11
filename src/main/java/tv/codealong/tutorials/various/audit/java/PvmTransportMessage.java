package tv.codealong.tutorials.various.audit.java;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public interface PvmTransportMessage {
    @NotNull
    String id();

    Object payload();

    byte[] payloadAsBytes();

    @Nullable
    String routeFieldValue(String var1);

    @NotNull
    String info();

    @Nullable
    String transportKey();

    @NotNull
    Map<String, String> headers();

    void putHeaders(@NotNull Map<String, String> var1);

    void removeHeaders(@NotNull Set<String> var1);

}
