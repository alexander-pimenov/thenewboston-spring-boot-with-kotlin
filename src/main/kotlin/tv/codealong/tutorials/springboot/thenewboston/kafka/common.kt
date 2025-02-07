package tv.codealong.tutorials.springboot.thenewboston.kafka

import org.springframework.context.SmartLifecycle
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class KafkaMessageListener(
    private val listenerContainer: KafkaMessageListenerContainer<*, *>, // Получаем контейнер listener
    private val apiClient: ApiClient // Клиент для вызова API Engine
) : SmartLifecycle {

    private val downloadedSchemas = ConcurrentHashMap<String, TestSchemaDeployment>()
    private var running = false

    // Управление жизненным циклом listener
    override fun isRunning() = running

    override fun start() {
        if (!running) {
            // Запускаем listener только после завершения загрузки схем
            println("Waiting for schemas to be loaded from API Engine...")
            val deploymentId = apiClient.loadModels() // Загружаем модели через WebClient
            downloadedSchemas[deploymentId] = TestSchemaDeployment(deploymentId)
            println("Schemas loaded. Starting Kafka listener.")
            listenerContainer.start() // Запускаем listener Kafka после загрузки
            running = true
        }
    }

    override fun stop() {
        if (running) {
            listenerContainer.stop() // Останавливаем listener
            running = false
        }
    }

    override fun isAutoStartup(): Boolean = false

    override fun getPhase(): Int = 0

    @KafkaListener(topics = ["bpmx-procces"], groupId = "my-group")
    fun listen(message: String) {
        val deploymentId = extractDeploymentIdFromMessage(message)
        if (downloadedSchemas.containsKey(deploymentId)) {
            println("Processing message: $message")
            // Выполнение действий после обработки сообщения
        } else {
            println("Skipping message for deploymentId: $deploymentId")
        }
    }

    private fun extractDeploymentIdFromMessage(message: String): String {
        // Логика извлечения deploymentId из сообщения
        return "some-deployment-id"
    }
}
