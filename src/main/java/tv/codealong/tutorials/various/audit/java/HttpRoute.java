package tv.codealong.tutorials.various.audit.java;

import feign.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Определяет конечные точки API
 *
 * Содержит имя маршрута, путь и метод
 *
 * Может включать специфичные для маршрута заголовки
 */
@Setter
@Getter
@ToString
public class HttpRoute {
    public String name;
    public String path;
    public Request.HttpMethod method;

    public HttpRoute(String name, String path, Request.HttpMethod method) {
        this.name = name;
        this.path = path;
        this.method = method;
    }

    protected boolean canEqual(Object other) {
        return other instanceof HttpRoute;
    }

}
