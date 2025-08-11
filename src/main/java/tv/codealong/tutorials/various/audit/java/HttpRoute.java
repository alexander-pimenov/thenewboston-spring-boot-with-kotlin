package tv.codealong.tutorials.various.audit.java;

import feign.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Определяет конечные точки API
 *
 * Содержит имя маршрута, путь и метод
 *
 * Может включать специфичные для маршрута заголовки
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpRoute {
    private String name;
    private String path;
    private Request.HttpMethod method;

    protected boolean canEqual(Object other) {
        return other instanceof HttpRoute;
    }
}
