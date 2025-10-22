package tv.codealong.tutorials.various.yandex.test5_1

// src/main/kotlin/com/example/services/UserService.kt

import java.time.LocalDateTime

interface UserRepository {
    fun findById(id: String): User?
    fun save(user: User): User
    fun findByEmail(email: String): User?
    fun delete(id: String): Boolean
}

interface EmailService {
    fun sendWelcomeEmail(user: User): Boolean
    fun sendPasswordResetEmail(email: String): Boolean
}

data class User(
    val id: String,
    val email: String,
    val name: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true
)

class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService
) {

    fun registerUser(email: String, name: String): User {
        // Проверяем что пользователь с таким email не существует
        val existingUser = userRepository.findByEmail(email)
        if (existingUser != null) {
            throw IllegalArgumentException("User with email $email already exists")
        }

        // Создаём нового пользователя
        val user = User(
            id = java.util.UUID.randomUUID().toString(),
            email = email,
            name = name
        )

        // Сохраняем в репозиторий
        val savedUser = userRepository.save(user)

        // Отправляем приветственное письмо
        emailService.sendWelcomeEmail(savedUser)

        return savedUser
    }

    fun deactivateUser(userId: String): Boolean {
        val user = userRepository.findById(userId) ?: return false

        val deactivatedUser = user.copy(isActive = false)
        userRepository.save(deactivatedUser)

        return true
    }

    fun requestPasswordReset(email: String): Boolean {
        val user = userRepository.findByEmail(email) ?: return false
        return emailService.sendPasswordResetEmail(user.email)
    }
}

// Ещё один сервис для демонстрации
class PaymentProcessor(
    private val fraudDetectionService: FraudDetectionService,
    private val transactionRepository: TransactionRepository
) {

    fun processPayment(userId: String, amount: Double, description: String): PaymentResult {
        // Проверяем на мошенничество
        val isFraudulent = fraudDetectionService.isSuspiciousTransaction(userId, amount)
        if (isFraudulent) {
            return PaymentResult.FraudDetected
        }

        // Создаём транзакцию
        val transaction = Transaction(
            id = java.util.UUID.randomUUID().toString(),
            userId = userId,
            amount = amount,
            description = description,
            status = TransactionStatus.PENDING
        )

        // Сохраняем транзакцию
        val savedTransaction = transactionRepository.save(transaction)

        // Имитируем обработку платежа
        Thread.sleep(100) // Имитация работы

        val completedTransaction = savedTransaction.copy(status = TransactionStatus.COMPLETED)
        transactionRepository.save(completedTransaction)

        return PaymentResult.Success(completedTransaction.id)
    }
}

interface FraudDetectionService {
    fun isSuspiciousTransaction(userId: String, amount: Double): Boolean
}

interface TransactionRepository {
    fun save(transaction: Transaction): Transaction
    fun findById(id: String): Transaction?
}

data class Transaction(
    val id: String,
    val userId: String,
    val amount: Double,
    val description: String,
    val status: TransactionStatus
)

enum class TransactionStatus {
    PENDING, COMPLETED, FAILED
}

sealed class PaymentResult {
    data class Success(val transactionId: String) : PaymentResult()
    object FraudDetected : PaymentResult()
    object InsufficientFunds : PaymentResult()
}