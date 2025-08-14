package tv.codealong.tutorials.various.audit.java;


import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
public interface MonitoringContext {
    @Nullable
    Map<String, String> getTagValues(String var1);

    void setTagValues(String var1, @Nullable Map<String, String> var2);
}
