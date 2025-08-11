package tv.codealong.tutorials.various.audit

import tv.codealong.tutorials.various.audit.java.HttpRoute

/**
 * Содержит базовую конфигурацию HTTP-клиента
 *
 * Управляет маршрутизацией через routeResolvers
 *
 * Хранит базовые заголовки и свойства
 */
class HttpSenderConfiguration {
    companion object {
        const val BASE_URL_PROPERTY = "base.url"
    }

    var name: String? = null
    var baseHeaders: Map<String, String> = emptyMap()
    var routeResolvers: List<TransportRouteConfig> = emptyList()
    var properties: Map<String, String> = emptyMap()
    private val routes: MutableMap<String, HttpRoute> = mutableMapOf()

    fun setRoutes(routes: List<HttpRoute>) {
        this.routes.clear()
        routes.forEach { this.routes[it.name] = it }
    }

    fun getRoute(name: String): HttpRoute? = routes[name]

    fun validate() {
        require(name?.isNotBlank() == true) { "Sender name must be specified" }
        require(properties.containsKey(BASE_URL_PROPERTY)) { "Base URL must be specified" }
    }
}

/**
 * Определяет конечные точки API
 *
 * Содержит имя маршрута, путь и метод
 *
 * Может включать специфичные для маршрута заголовки
 */
//data class HttpRoute(
//    val name: String,
//    val path: String,
//    val method: HttpMethod,
//    val headers: Map<String, String> = emptyMap()
//) {
//    init {
//        require(name.isNotBlank()) { "Route name must not be blank" }
//        require(path.isNotBlank()) { "Path must not be blank" }
//    }
//}
//
//enum class HttpMethod {
//    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
//}

//можно и такой класс сделать:
//class Request {
//    enum class HttpMethod {
//        GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
//    }
//}