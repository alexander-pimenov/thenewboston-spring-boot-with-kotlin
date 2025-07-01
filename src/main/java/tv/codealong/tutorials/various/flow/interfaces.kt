package tv.codealong.tutorials.various.flow

// Интерфейс модели вспомогательного потока
interface AuxiliaryFlowModel {
    // Свойство, возвращающее объект типа SberFlowModel
    val sfModel: SberFlowModel

    // Свойство, возвращающее режим вспомогательного потока
    val mode: AuxiliaryFlowMode
}

// Интерфейс основной модели потока от Сбербанка
interface SberFlowModel {
    // Уникальный идентификатор модели
    val sfModelId: String
}

// Перечисление режимов вспомогательных потоков
enum class AuxiliaryFlowMode {
    DEFAULT,   // Стандартный режим
    IDEMPOTENT // Идемпотентный режим (однократность действий)
}