package tv.codealong.tutorials.various.audit.java;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransportRouteConfig {
    private String field;
    private Pattern pattern;
    private String route;
    private boolean isDefault;

    protected boolean canEqual(Object other) {
        return other instanceof TransportRouteConfig;
    }
}
