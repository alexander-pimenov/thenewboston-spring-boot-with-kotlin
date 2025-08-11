package tv.codealong.tutorials.various.audit.java;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SenderConfiguration {
    private String name;
    private List<TransportRouteConfig> routeResolvers;
    private Map<String, String> baseHeaders;

    protected boolean canEqual(Object other) {
        return other instanceof SenderConfiguration;
    }
}
