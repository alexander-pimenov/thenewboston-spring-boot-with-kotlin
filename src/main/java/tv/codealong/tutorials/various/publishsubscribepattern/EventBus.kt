package tv.codealong.tutorials.various.publishsubscribepattern

import kotlin.reflect.KClass

/**
 * Шаблон "публикация-подписка" (publish-subscribe, pub/sub) — это архитектурный паттерн обмена сообщениями,
 * который отделяет отправителей сообщений (издателей) от получателей (подписчиков) через посредника — брокера
 * сообщений или шину событий. Издатели публикуют сообщения в определённые темы (topics), а подписчики подписываются
 * на эти темы, получая только интересующие их сообщения. Такой подход снижает связанность компонентов, улучшает
 * масштабируемость и гибкость системы, так как издатели и подписчики не знают напрямую друг о друге.
 *
 * Пример из жизни — газета и её читатели: издатель публикует газетные выпуски (сообщения), а подписчики получают
 * только те выпуски, на которые подписались, без необходимости знать об издателе. В реальных приложениях это используется
 * для событийно-управляемой архитектуры, микросервисов, систем обмена сообщениями (Kafka, RabbitMQ, Google Cloud Pub/Sub и др.).
 *
 * В этом примере есть объект EventBus — брокер сообщений, который хранит подписчиков для разных типов сообщений.
 * Издатель вызывает publish, и все подписчики этого типа получают событие. Подписчики регистрируются через subscribe
 * с обработчиками событий. Такой паттерн поддерживает слабую связанность и типизированную обработку сообщений.
 *
 * В этом примере:
 *
 * EventBus позволяет подписываться на несколько типов сообщений.
 *
 * Для одного типа Message зарегистрировано два подписчика, которые получают и обрабатывают сообщения по-своему.
 *
 * Есть подписчики и для других типов событий Notification и Alert.
 *
 * При вызове publish каждый подписчик этого типа получает событие.
 *
 * Такое расширение демонстрирует, как паттерн публикация-подписка поддерживает множественных подписчиков и
 * продюсеров одновременно, позволяя разным частям приложения независимо публиковать и обрабатывать события без жесткой связи между ними. Это основа для построения реактивной, масштабируемой системы обмена сообщениями на Kotlin.
 *
 */
object EventBus {
    val subscribers = mutableMapOf<KClass<*>, MutableList<(Any) -> Unit>>()

    inline fun <reified T : Any> subscribe(noinline handler: (T) -> Unit) {
        subscribers.getOrPut(T::class) { mutableListOf() }.add { event -> handler(event as T) }
    }

    inline fun <reified T : Any> publish(event: T) {
        subscribers[T::class]?.forEach { handler -> handler(event) }
    }
}

// Определим разные типы событий
data class Message(val text: String)
data class Notification(val title: String, val content: String)
data class Alert(val level: Int, val message: String)

fun main() {
    // Подписчик на Message
    EventBus.subscribe<Message> { msg ->
        println("Subscriber 1 (Message): ${msg.text}")
    }

    // Еще один подписчик на Message
    EventBus.subscribe<Message> { msg ->
        println("Subscriber 2 (Message): ${msg.text.uppercase()}")
    }

    // Подписчик на Notification
    EventBus.subscribe<Notification> { notification ->
        println("Subscriber (Notification): ${notification.title} - ${notification.content}")
    }

    // Подписчик на Alert
    EventBus.subscribe<Alert> { alert ->
        println("Subscriber (Alert): Level ${alert.level} - ${alert.message}")
    }

    // Продюсеры публикуют разные события
    EventBus.publish(Message("Hello from producer 1"))
    EventBus.publish(Notification("News", "EventBus pattern in Kotlin"))
    EventBus.publish(Message("Another message from producer 2"))
    EventBus.publish(Alert(3, "High CPU usage"))
    println("Done.")

    println("Посмотрим что внутри:")
    println(EventBus.subscribers)
}
