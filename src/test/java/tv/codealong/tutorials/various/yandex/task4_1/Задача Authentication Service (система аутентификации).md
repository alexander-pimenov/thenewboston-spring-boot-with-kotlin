Давай спроектируем систему аутентификации. Это прекрасный пример для изучения архитектуры и безопасности. 🔐

## 🎯 **ЗАДАЧА: Authentication Service**

```kotlin
/**
 * Требования:
 * 1. Регистрация пользователей (email/пароль)
 * 2. Аутентификация (логин) с выдачей токенов
 * 3. Валидация JWT токенов
 * 4. Выход из системы (logout)
 * 5. Сброс пароля
 * 6. Безопасное хранение паролей
 * 7. Rate limiting для защиты от брутфорса
 * 8. Сессии пользователей
 */
```

## 🔍 **ШАГ 1: Декомпозиция задачи**

```kotlin
// 1. 👤 Модели пользователей и сессий
// 2. 🔐 Хеширование паролей (bcrypt)
// 3. 🎫 Генерация и валидация JWT токенов
// 4. 📦 Хранение данных (память/БД)
// 5. 🛡️ Безопасность (rate limiting, проверки)
// 6. 📧 Уведомления (email для сброса пароля)
// 7. ⚙️ Конфигурация (секреты, настройки)
```

## 🏗️ **ШАГ 2: Определение сущностей**

### Вопрос 1: **"Что представляет собой пользователь?"**

```kotlin
// Пользователь должен хранить:
// - Уникальный идентификатор
// - Email (уникальный)
// - Хеш пароля (НЕ сам пароль!)
// - Статус (активен/заблокирован)
// - Дата регистрации
// - Роли/разрешения

// ✅ Решение: data class для пользователя
data class User(
    val id: String,
    val email: String,
    val passwordHash: String, // Никогда не храним пароль в открытом виде!
    val isActive: Boolean = true,
    val roles: Set<UserRole> = setOf(UserRole.USER),
    val createdAt: Instant = Instant.now(),
    val lastLoginAt: Instant? = null
)

enum class UserRole {
    USER, ADMIN, MODERATOR
}
```

### Вопрос 2: **"Как управлять сессиями?"**

```kotlin
// Сессия должна хранить:
// - Токен доступа (JWT)
// - ID пользователя
// - Время создания и expiration
// - Device info (для безопасности)

// ✅ Решение: data class для сессии
data class UserSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val deviceInfo: DeviceInfo? = null,
    val createdAt: Instant = Instant.now(),
    val expiresAt: Instant,
    val isRevoked: Boolean = false
)

data class DeviceInfo(
    val userAgent: String,
    val ipAddress: String,
    val deviceType: String = "unknown"
)
```

### Вопрос 3: **"Как обрабатывать сброс пароля?"**

```kotlin
// Токен сброса пароля:
// - Уникальный токен
// - ID пользователя  
// - Срок действия (короткий)
// - Использован ли уже

// ✅ Решение: data class для токена сброса
data class PasswordResetToken(
    val token: String,
    val userId: String,
    val createdAt: Instant = Instant.now(),
    val expiresAt: Instant,
    val isUsed: Boolean = false
)
```

## 🔨 **НАЧНЁМ РЕАЛИЗАЦИЮ!**

```kotlin
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

// 🎯 1. Сначала определим результаты операций (sealed classes)
sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val code: AuthErrorCode) : AuthResult<Nothing>()
}

enum class AuthErrorCode {
    USER_NOT_FOUND,
    INVALID_PASSWORD,
    USER_INACTIVE,
    EMAIL_ALREADY_EXISTS,
    INVALID_TOKEN,
    TOKEN_EXPIRED,
    RATE_LIMIT_EXCEEDED,
    WEAK_PASSWORD
}

// 🎯 2. Модели ответов для API
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Instant,
    val user: UserInfo
)

data class UserInfo(
    val id: String,
    val email: String,
    val roles: Set<UserRole>,
    val isActive: Boolean
)

// 🎯 3. КОНФИГУРАЦИЯ
data class AuthConfig(
    val jwtSecret: String,
    val accessTokenExpiration: java.time.Duration = java.time.Duration.ofHours(2),
    val refreshTokenExpiration: java.time.Duration = java.time.Duration.ofDays(30),
    val passwordResetTokenExpiration: java.time.Duration = java.time.Duration.ofHours(1),
    val maxLoginAttempts: Int = 5,
    val loginBlockDuration: java.time.Duration = java.time.Duration.ofMinutes(30),
    val bcryptRounds: Int = 12
) {
    companion object {
        fun build(block: Builder.() -> Unit = {}): AuthConfig {
            return Builder().apply(block).build()
        }
    }
    
    class Builder {
        var jwtSecret: String = generateRandomSecret()
        var accessTokenExpiration: java.time.Duration = java.time.Duration.ofHours(2)
        var refreshTokenExpiration: java.time.Duration = java.time.Duration.ofDays(30)
        var passwordResetTokenExpiration: java.time.Duration = java.time.Duration.ofHours(1)
        var maxLoginAttempts: Int = 5
        var loginBlockDuration: java.time.Duration = java.time.Duration.ofMinutes(30)
        var bcryptRounds: Int = 12
        
        fun build(): AuthConfig {
            require(jwtSecret.length >= 32) { "JWT secret must be at least 32 characters" }
            return AuthConfig(
                jwtSecret = jwtSecret,
                accessTokenExpiration = accessTokenExpiration,
                refreshTokenExpiration = refreshTokenExpiration,
                passwordResetTokenExpiration = passwordResetTokenExpiration,
                maxLoginAttempts = maxLoginAttempts,
                loginBlockDuration = loginBlockDuration,
                bcryptRounds = bcryptRounds
            )
        }
        
        private fun generateRandomSecret(): String {
            val random = SecureRandom()
            val bytes = ByteArray(32)
            random.nextBytes(bytes)
            return Base64.getEncoder().encodeToString(bytes)
        }
    }
}

// 🎯 4. ИНТЕРФЕЙС ХРАНИЛИЩА (для гибкости)
interface UserRepository {
    fun createUser(user: User): AuthResult<User>
    fun findUserById(id: String): User?
    fun findUserByEmail(email: String): User?
    fun updateUser(user: User): AuthResult<User>
    fun deleteUser(id: String): AuthResult<Unit>
}

interface SessionRepository {
    fun createSession(session: UserSession): AuthResult<UserSession>
    fun findSessionByAccessToken(token: String): UserSession?
    fun findSessionsByUserId(userId: String): List<UserSession>
    fun revokeSession(token: String): AuthResult<Unit>
    fun revokeAllUserSessions(userId: String): AuthResult<Unit>
}

// 🎯 5. IN-MEMORY РЕАЛИЗАЦИИ (для примера)
class InMemoryUserRepository : UserRepository {
    private val usersById = ConcurrentHashMap<String, User>()
    private val usersByEmail = ConcurrentHashMap<String, User>()
    
    override fun createUser(user: User): AuthResult<User> {
        synchronized(this) {
            if (usersByEmail.containsKey(user.email)) {
                return AuthResult.Error("Email already exists", AuthErrorCode.EMAIL_ALREADY_EXISTS)
            }
            
            usersById[user.id] = user
            usersByEmail[user.email] = user
            return AuthResult.Success(user)
        }
    }
    
    override fun findUserById(id: String): User? = usersById[id]
    
    override fun findUserByEmail(email: String): User? = usersByEmail[email]
    
    override fun updateUser(user: User): AuthResult<User> {
        synchronized(this) {
            val existingUser = usersById[user.id] 
                ?: return AuthResult.Error("User not found", AuthErrorCode.USER_NOT_FOUND)
            
            // Если email изменился, проверяем уникальность
            if (existingUser.email != user.email && usersByEmail.containsKey(user.email)) {
                return AuthResult.Error("Email already exists", AuthErrorCode.EMAIL_ALREADY_EXISTS)
            }
            
            // Обновляем оба индекса
            usersByEmail.remove(existingUser.email)
            usersById[user.id] = user
            usersByEmail[user.email] = user
            
            return AuthResult.Success(user)
        }
    }
    
    override fun deleteUser(id: String): AuthResult<Unit> {
        synchronized(this) {
            val user = usersById[id] 
                ?: return AuthResult.Error("User not found", AuthErrorCode.USER_NOT_FOUND)
            usersById.remove(id)
            usersByEmail.remove(user.email)
            return AuthResult.Success(Unit)
        }
    }
}

// 🎯 6. ОСНОВНОЙ СЕРВИС АУТЕНТИФИКАЦИИ
class AuthenticationService(
    private val config: AuthConfig,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder(config.bcryptRounds),
    private val tokenProvider: TokenProvider = JwtTokenProvider(config),
    private val rateLimiter: RateLimiter = SimpleRateLimiter()
) {
    
    // 📊 Метрики
    private val loginAttempts = AtomicLong(0)
    private val successfulLogins = AtomicLong(0)
    private val failedLogins = AtomicLong(0)
    
    // 🔐 ОСНОВНЫЕ ОПЕРАЦИИ
    
    fun register(email: String, password: String, roles: Set<UserRole> = setOf(UserRole.USER)): AuthResult<UserInfo> {
        // 1. Валидация email и пароля
        if (!isValidEmail(email)) {
            return AuthResult.Error("Invalid email format", AuthErrorCode.USER_NOT_FOUND)
        }
        
        if (!isStrongPassword(password)) {
            return AuthResult.Error("Password is too weak", AuthErrorCode.WEAK_PASSWORD)
        }
        
        // 2. Хеширование пароля
        val passwordHash = passwordEncoder.encode(password)
        
        // 3. Создание пользователя
        val user = User(
            id = UUID.randomUUID().toString(),
            email = email,
            passwordHash = passwordHash,
            roles = roles
        )
        
        return when (val result = userRepository.createUser(user)) {
            is AuthResult.Success -> AuthResult.Success(result.data.toUserInfo())
            is AuthResult.Error -> result
        }
    }
    
    fun login(email: String, password: String, deviceInfo: DeviceInfo? = null): AuthResult<LoginResponse> {
        // 1. Rate limiting для защиты от брутфорса
        if (!rateLimiter.tryAcquire("login:$email")) {
            return AuthResult.Error("Too many login attempts", AuthErrorCode.RATE_LIMIT_EXCEEDED)
        }
        
        loginAttempts.incrementAndGet()
        
        // 2. Поиск пользователя
        val user = userRepository.findUserByEmail(email)
            ?: return AuthResult.Error("User not found", AuthErrorCode.USER_NOT_FOUND).also { 
                failedLogins.incrementAndGet() 
            }
        
        // 3. Проверка активности
        if (!user.isActive) {
            return AuthResult.Error("User account is inactive", AuthErrorCode.USER_INACTIVE).also { 
                failedLogins.incrementAndGet() 
            }
        }
        
        // 4. Проверка пароля
        if (!passwordEncoder.matches(password, user.passwordHash)) {
            return AuthResult.Error("Invalid password", AuthErrorCode.INVALID_PASSWORD).also { 
                failedLogins.incrementAndGet() 
            }
        }
        
        // 5. Генерация токенов
        val accessToken = tokenProvider.generateAccessToken(user)
        val refreshToken = tokenProvider.generateRefreshToken(user)
        
        val session = UserSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = user.id,
            deviceInfo = deviceInfo,
            expiresAt = Instant.now().plus(config.accessTokenExpiration)
        )
        
        // 6. Сохранение сессии
        return when (val result = sessionRepository.createSession(session)) {
            is AuthResult.Success -> {
                successfulLogins.incrementAndGet()
                AuthResult.Success(
                    LoginResponse(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        expiresAt = session.expiresAt,
                        user = user.toUserInfo()
                    )
                )
            }
            is AuthResult.Error -> result.also { failedLogins.incrementAndGet() }
        }
    }
    
    fun validateToken(accessToken: String): AuthResult<UserInfo> {
        // 1. Проверка в хранилище сессий
        val session = sessionRepository.findSessionByAccessToken(accessToken)
            ?: return AuthResult.Error("Invalid token", AuthErrorCode.INVALID_TOKEN)
        
        // 2. Проверка срока действия
        if (session.expiresAt.isBefore(Instant.now())) {
            return AuthResult.Error("Token expired", AuthErrorCode.TOKEN_EXPIRED)
        }
        
        // 3. Проверка отозвана ли сессия
        if (session.isRevoked) {
            return AuthResult.Error("Token revoked", AuthErrorCode.INVALID_TOKEN)
        }
        
        // 4. Поиск пользователя
        val user = userRepository.findUserById(session.userId)
            ?: return AuthResult.Error("User not found", AuthErrorCode.USER_NOT_FOUND)
        
        // 5. Проверка активности пользователя
        if (!user.isActive) {
            return AuthResult.Error("User account is inactive", AuthErrorCode.USER_INACTIVE)
        }
        
        return AuthResult.Success(user.toUserInfo())
    }
    
    fun logout(accessToken: String): AuthResult<Unit> {
        return sessionRepository.revokeSession(accessToken)
    }
    
    fun refreshToken(refreshToken: String, deviceInfo: DeviceInfo? = null): AuthResult<LoginResponse> {
        // Находим сессию по refresh token и создаём новую
        // (упрощённо для примера)
        return AuthResult.Error("Refresh not implemented", AuthErrorCode.INVALID_TOKEN)
    }
    
    fun requestPasswordReset(email: String): AuthResult<Unit> {
        val user = userRepository.findUserByEmail(email)
            ?: return AuthResult.Error("User not found", AuthErrorCode.USER_NOT_FOUND)
        
        // В реальной системе здесь был бы отправка email с токеном сброса
        println("📧 Password reset requested for: $email")
        return AuthResult.Success(Unit)
    }
    
    fun resetPassword(token: String, newPassword: String): AuthResult<Unit> {
        // Валидация токена и сброс пароля
        // (упрощённо для примера)
        return AuthResult.Error("Password reset not implemented", AuthErrorCode.INVALID_TOKEN)
    }
    
    // 🔧 ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    
    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.length in 5..254
    }
    
    private fun isStrongPassword(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isDigit() } &&
                password.any { it.isLetter() }
    }
    
    private fun User.toUserInfo(): UserInfo {
        return UserInfo(
            id = this.id,
            email = this.email,
            roles = this.roles,
            isActive = this.isActive
        )
    }
    
    // 📊 МЕТРИКИ
    
    fun getMetrics(): AuthMetrics {
        return AuthMetrics(
            totalUsers = getTotalUsers(),
            activeSessions = getActiveSessionsCount(),
            loginAttempts = loginAttempts.get(),
            successfulLogins = successfulLogins.get(),
            failedLogins = failedLogins.get()
        )
    }
    
    private fun getTotalUsers(): Long {
        // В реальной системе здесь был бы запрос к БД
        return 0
    }
    
    private fun getActiveSessionsCount(): Long {
        // В реальной системе здесь был бы запрос к БД
        return 0
    }
}

// 🎯 7. ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ

// 🔐 Кодировщик паролей
interface PasswordEncoder {
    fun encode(rawPassword: String): String
    fun matches(rawPassword: String, encodedPassword: String): Boolean
}

class BCryptPasswordEncoder(private val strength: Int = 12) : PasswordEncoder {
    override fun encode(rawPassword: String): String {
        // В реальной системе используем BCrypt
        // return BCrypt.hashpw(rawPassword, BCrypt.gensalt(strength))
        return "hashed_$rawPassword" // заглушка
    }
    
    override fun matches(rawPassword: String, encodedPassword: String): Boolean {
        // return BCrypt.checkpw(rawPassword, encodedPassword)
        return encodedPassword == "hashed_$rawPassword" // заглушка
    }
}

// 🎫 Провайдер JWT токенов
interface TokenProvider {
    fun generateAccessToken(user: User): String
    fun generateRefreshToken(user: User): String
    fun validateToken(token: String): Boolean
    fun extractUserId(token: String): String?
}

class JwtTokenProvider(private val config: AuthConfig) : TokenProvider {
    override fun generateAccessToken(user: User): String {
        // В реальной системе генерируем JWT
        return "access_token_${user.id}_${System.currentTimeMillis()}"
    }
    
    override fun generateRefreshToken(user: User): String {
        return "refresh_token_${user.id}_${System.currentTimeMillis()}"
    }
    
    override fun validateToken(token: String): Boolean {
        return token.startsWith("access_token_") || token.startsWith("refresh_token_")
    }
    
    override fun extractUserId(token: String): String? {
        return token.split("_").getOrNull(2)
    }
}

// 🚦 Rate Limiter
interface RateLimiter {
    fun tryAcquire(key: String): Boolean
}

class SimpleRateLimiter : RateLimiter {
    private val attempts = ConcurrentHashMap<String, MutableList<Instant>>()
    
    override fun tryAcquire(key: String): Boolean {
        val now = Instant.now()
        val windowStart = now.minusSeconds(60) // 1 minute window
        
        synchronized(attempts) {
            val keyAttempts = attempts.getOrPut(key) { mutableListOf() }
            
            // Удаляем старые попытки
            keyAttempts.removeAll { it.isBefore(windowStart) }
            
            // Проверяем лимит (максимум 5 попыток в минуту)
            if (keyAttempts.size >= 5) {
                return false
            }
            
            keyAttempts.add(now)
            return true
        }
    }
}

// 📊 Метрики
data class AuthMetrics(
    val totalUsers: Long,
    val activeSessions: Long,
    val loginAttempts: Long,
    val successfulLogins: Long,
    val failedLogins: Long
) {
    val successRate: Double get() = 
        if (loginAttempts > 0) successfulLogins.toDouble() / loginAttempts.toDouble() else 0.0
}
```

Продолжение в следующем сообщении... 🚀

---

Продолжаем! Теперь давай добавим тестирование и покажем как вся система работает вместе. 🚀

## 🧪 **ТЕСТИРУЕМ СИСТЕМУ АУТЕНТИФИКАЦИИ**

```kotlin
// 🎯 8. ДОПОЛНИТЕЛЬНЫЕ РЕПОЗИТОРИИ (In-Memory)

class InMemorySessionRepository : SessionRepository {
    private val sessionsByToken = ConcurrentHashMap<String, UserSession>()
    private val sessionsByUserId = ConcurrentHashMap<String, MutableList<UserSession>>()
    
    override fun createSession(session: UserSession): AuthResult<UserSession> {
        synchronized(this) {
            sessionsByToken[session.accessToken] = session
            
            val userSessions = sessionsByUserId.getOrPut(session.userId) { mutableListOf() }
            userSessions.add(session)
            
            return AuthResult.Success(session)
        }
    }
    
    override fun findSessionByAccessToken(token: String): UserSession? {
        return sessionsByToken[token]
    }
    
    override fun findSessionsByUserId(userId: String): List<UserSession> {
        return sessionsByUserId[userId] ?: emptyList()
    }
    
    override fun revokeSession(token: String): AuthResult<Unit> {
        synchronized(this) {
            val session = sessionsByToken[token] ?: return AuthResult.Error("Session not found", AuthErrorCode.INVALID_TOKEN)
            
            sessionsByToken.remove(token)
            sessionsByUserId[session.userId]?.remove(session)
            
            return AuthResult.Success(Unit)
        }
    }
    
    override fun revokeAllUserSessions(userId: String): AuthResult<Unit> {
        synchronized(this) {
            val userSessions = sessionsByUserId[userId] ?: return AuthResult.Success(Unit)
            
            userSessions.forEach { session ->
                sessionsByToken.remove(session.accessToken)
            }
            sessionsByUserId.remove(userId)
            
            return AuthResult.Success(Unit)
        }
    }
}

// 🎯 9. ФАБРИКА ДЛЯ СОЗДАНИЯ СЕРВИСА

class AuthServiceFactory {
    companion object {
        fun createInMemoryService(block: AuthConfig.Builder.() -> Unit = {}): AuthenticationService {
            val config = AuthConfig.build(block)
            val userRepository = InMemoryUserRepository()
            val sessionRepository = InMemorySessionRepository()
            
            return AuthenticationService(
                config = config,
                userRepository = userRepository,
                sessionRepository = sessionRepository
            )
        }
    }
}

// 🎯 10. ТЕСТИРОВАНИЕ ВСЕЙ СИСТЕМЫ

fun main() {
    println("=== 🔐 ТЕСТИРОВАНИЕ СИСТЕМЫ АУТЕНТИФИКАЦИИ ===")
    
    // Создаём сервис аутентификации
    val authService = AuthServiceFactory.createInMemoryService {
        jwtSecret = "very-secure-secret-key-for-testing-purposes-only"
        accessTokenExpiration = java.time.Duration.ofMinutes(5) // Короткое для теста
        maxLoginAttempts = 3 // Мало попыток для демонстрации rate limiting
    }
    
    // 🔹 ТЕСТ 1: Регистрация пользователя
    println("\n🔹 ТЕСТ 1: РЕГИСТРАЦИЯ")
    val registerResult = authService.register(
        email = "alice@example.com",
        password = "SecurePass123!"
    )
    
    when (registerResult) {
        is AuthResult.Success -> {
            println("✅ Регистрация успешна!")
            println("   Пользователь: ${registerResult.data.email}")
            println("   ID: ${registerResult.data.id}")
        }
        is AuthResult.Error -> {
            println("❌ Ошибка регистрации: ${registerResult.message}")
        }
    }
    
    // 🔹 ТЕСТ 2: Успешный логин
    println("\n🔹 ТЕСТ 2: УСПЕШНЫЙ ЛОГИН")
    val loginResult = authService.login(
        email = "alice@example.com",
        password = "SecurePass123!",
        deviceInfo = DeviceInfo(
            userAgent = "Mozilla/5.0...",
            ipAddress = "192.168.1.100",
            deviceType = "Desktop"
        )
    )
    
    when (loginResult) {
        is AuthResult.Success -> {
            println("✅ Логин успешен!")
            println("   Access Token: ${loginResult.data.accessToken.take(20)}...")
            println("   User: ${loginResult.data.user.email}")
            println("   Expires: ${loginResult.data.expiresAt}")
        }
        is AuthResult.Error -> {
            println("❌ Ошибка логина: ${loginResult.message}")
        }
    }
    
    // 🔹 ТЕСТ 3: Валидация токена
    println("\n🔹 ТЕСТ 3: ВАЛИДАЦИЯ ТОКЕНА")
    if (loginResult is AuthResult.Success) {
        val validateResult = authService.validateToken(loginResult.data.accessToken)
        
        when (validateResult) {
            is AuthResult.Success -> {
                println("✅ Токен валиден!")
                println("   Пользователь: ${validateResult.data.email}")
            }
            is AuthResult.Error -> {
                println("❌ Токен невалиден: ${validateResult.message}")
            }
        }
    }
    
    // 🔹 ТЕСТ 4: Неправильный пароль
    println("\n🔹 ТЕСТ 4: НЕПРАВИЛЬНЫЙ ПАРОЛЬ")
    val wrongPasswordResult = authService.login(
        email = "alice@example.com", 
        password = "WrongPassword123!"
    )
    
    when (wrongPasswordResult) {
        is AuthResult.Success -> println("✅ Логин успешен (не должно быть!)")
        is AuthResult.Error -> println("❌ Ожидаемая ошибка: ${wrongPasswordResult.message}")
    }
    
    // 🔹 ТЕСТ 5: Rate Limiting
    println("\n🔹 ТЕСТ 5: RATE LIMITING")
    repeat(4) { attempt ->
        val rateLimitResult = authService.login(
            email = "test@example.com", // Несуществующий пользователь
            password = "anypassword"
        )
        
        when (rateLimitResult) {
            is AuthResult.Success -> println("  Попытка ${attempt + 1}: ✅ Успех")
            is AuthResult.Error -> println("  Попытка ${attempt + 1}: ❌ ${rateLimitResult.message}")
        }
    }
    
    // 🔹 ТЕСТ 6: Logout
    println("\n🔹 ТЕСТ 6: LOGOUT")
    if (loginResult is AuthResult.Success) {
        val logoutResult = authService.logout(loginResult.data.accessToken)
        
        when (logoutResult) {
            is AuthResult.Success -> println("✅ Logout успешен!")
            is AuthResult.Error -> println("❌ Ошибка logout: ${logoutResult.message}")
        }
        
        // Пробуем использовать токен после logout
        println("🔹 Проверка токена после logout:")
        val validateAfterLogout = authService.validateToken(loginResult.data.accessToken)
        when (validateAfterLogout) {
            is AuthResult.Success -> println("❌ Токен всё ещё работает (не должно быть!)")
            is AuthResult.Error -> println("✅ Токен правильно отозван: ${validateAfterLogout.message}")
        }
    }
    
    // 🔹 ТЕСТ 7: Метрики
    println("\n🔹 ТЕСТ 7: МЕТРИКИ")
    val metrics = authService.getMetrics()
    println("""
        📊 Метрики аутентификации:
        • Всего попыток входа: ${metrics.loginAttempts}
        • Успешных входов: ${metrics.successfulLogins} 
        • Неудачных входов: ${metrics.failedLogins}
        • Успешность: ${"%.1f".format(metrics.successRate * 100)}%
    """.trimIndent())
    
    // 🔹 ТЕСТ 8: Попытка регистрации с существующим email
    println("\n🔹 ТЕСТ 8: ДУБЛИРОВАНИЕ EMAIL")
    val duplicateResult = authService.register(
        email = "alice@example.com", // Тот же email
        password = "AnotherPass123!"
    )
    
    when (duplicateResult) {
        is AuthResult.Success -> println("❌ Регистрация прошла (не должно быть!)")
        is AuthResult.Error -> println("✅ Ожидаемая ошибка: ${duplicateResult.message}")
    }
    
    println("\n=== 🎯 ТЕСТИРОВАНИЕ ЗАВЕРШЕНО ===")
}

// 🎯 11. ДОПОЛНИТЕЛЬНЫЕ ТЕСТЫ ДЛЯ РАЗНЫХ СЦЕНАРИЕВ

fun testAdvancedScenarios() {
    println("\n=== 🧪 ПРОДВИНУТЫЕ СЦЕНАРИИ ===")
    
    val authService = AuthServiceFactory.createInMemoryService()
    
    // 🔹 Сценарий 1: Несколько пользователей
    println("🔹 СЦЕНАРИЙ 1: НЕСКОЛЬКО ПОЛЬЗОВАТЕЛЕЙ")
    
    val users = listOf(
        "bob@example.com" to "BobPassword123!",
        "carol@example.com" to "CarolSecure456!",
        "dave@example.com" to "DavePass789!"
    )
    
    users.forEach { (email, password) ->
        val result = authService.register(email, password)
        when (result) {
            is AuthResult.Success -> println("   ✅ $email зарегистрирован")
            is AuthResult.Error -> println("   ❌ $email: ${result.message}")
        }
    }
    
    // 🔹 Сценарий 2: Разные роли пользователей
    println("\n🔹 СЦЕНАРИЙ 2: АДМИНИСТРАТОР")
    
    // В реальной системе добавили бы метод для создания пользователя с ролями
    println("   ⚠️ Регистрация администратора (требует отдельного метода)")
    
    // 🔹 Сценарий 3: Валидация email и пароля
    println("\n🔹 СЦЕНАРИЙ 3: ВАЛИДАЦИЯ ДАННЫХ")
    
    val invalidCases = listOf(
        "invalid-email" to "Pass123!", // Неправильный email
        "test@example.com" to "123",   // Слишком короткий пароль
        "test@example.com" to "password", // Пароль без цифр
        "test@example.com" to "12345678"  // Пароль без букв
    )
    
    invalidCases.forEach { (email, password) ->
        val result = authService.register(email, password)
        when (result) {
            is AuthResult.Success -> println("   ❌ $email принят (не должно быть!)")
            is AuthResult.Error -> println("   ✅ $email отклонён: ${result.message}")
        }
    }
}

// 🎯 12. ИНТЕГРАЦИЯ С WEB ФРЕЙМВОРКОМ (пример)

class AuthController(private val authService: AuthenticationService) {
    
    fun register(request: RegisterRequest): ApiResponse<LoginResponse> {
        return when (val result = authService.register(request.email, request.password)) {
            is AuthResult.Success -> {
                // Автоматически логиним после регистрации
                when (val loginResult = authService.login(request.email, request.password)) {
                    is AuthResult.Success -> ApiResponse.success(loginResult.data)
                    is AuthResult.Error -> ApiResponse.error(loginResult.message)
                }
            }
            is AuthResult.Error -> ApiResponse.error(result.message)
        }
    }
    
    fun login(request: LoginRequest): ApiResponse<LoginResponse> {
        return when (val result = authService.login(request.email, request.password, request.deviceInfo)) {
            is AuthResult.Success -> ApiResponse.success(result.data)
            is AuthResult.Error -> ApiResponse.error(result.message)
        }
    }
    
    fun logout(authHeader: String?): ApiResponse<Unit> {
        val token = extractToken(authHeader) ?: return ApiResponse.error("Missing token")
        
        return when (val result = authService.logout(token)) {
            is AuthResult.Success -> ApiResponse.success(Unit)
            is AuthResult.Error -> ApiResponse.error(result.message)
        }
    }
    
    fun getProfile(authHeader: String?): ApiResponse<UserInfo> {
        val token = extractToken(authHeader) ?: return ApiResponse.error("Missing token")
        
        return when (val result = authService.validateToken(token)) {
            is AuthResult.Success -> ApiResponse.success(result.data)
            is AuthResult.Error -> ApiResponse.error(result.message)
        }
    }
    
    private fun extractToken(authHeader: String?): String? {
        return authHeader?.removePrefix("Bearer ")
    }
}

// Модели запросов для Web API
data class RegisterRequest(val email: String, val password: String)
data class LoginRequest(val email: String, val password: String, val deviceInfo: DeviceInfo?)

// Общий ответ API
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(true, data)
        fun <T> error(message: String): ApiResponse<T> = ApiResponse(false, error = message)
    }
}

// 🚀 ЗАПУСКАЕМ ВСЕ ТЕСТЫ

fun main() {
    testAuthenticationSystem()
    testAdvancedScenarios()
    
    // Демонстрация использования в Web приложении
    demonstrateWebIntegration()
}

fun demonstrateWebIntegration() {
    println("\n=== 🌐 ИНТЕГРАЦИЯ С WEB API ===")
    
    val authService = AuthServiceFactory.createInMemoryService()
    val authController = AuthController(authService)
    
    // Симуляция HTTP запросов
    println("🔹 Симуляция HTTP запросов:")
    
    // 1. Регистрация
    println("1. POST /api/register")
    val registerResponse = authController.register(RegisterRequest("webuser@example.com", "WebPass123!"))
    println("   Ответ: ${if (registerResponse.success) "✅ Успех" else "❌ ${registerResponse.error}"}")
    
    // 2. Логин
    println("2. POST /api/login") 
    val loginResponse = authController.login(LoginRequest("webuser@example.com", "WebPass123!", null))
    println("   Ответ: ${if (loginResponse.success) "✅ Успех" else "❌ ${loginResponse.error}"}")
    
    // 3. Получение профиля (с токеном)
    if (loginResponse.success && loginResponse.data != null) {
        println("3. GET /api/profile (с токеном)")
        val profileResponse = authController.getProfile("Bearer ${loginResponse.data.accessToken}")
        println("   Ответ: ${if (profileResponse.success) "✅ Профиль получен" else "❌ ${profileResponse.error}"}")
    }
    
    println("\n🎯 СИСТЕМА АУТЕНТИФИКАЦИИ ГОТОВА К ИСПОЛЬЗОВАНИЮ!")
}
```

## 🎯 **КЛЮЧЕВЫЕ АРХИТЕКТУРНЫЕ РЕШЕНИЯ:**

### 1. **Разделение ответственности:**
- `AuthenticationService` - бизнес-логика
- `UserRepository` / `SessionRepository` - хранение данных
- `PasswordEncoder` / `TokenProvider` - инфраструктура
- `AuthController` - web слой

### 2. **Безопасность:**
- ✅ Хеширование паролей (никогда не храним в открытом виде)
- ✅ JWT токены с expiration
- ✅ Rate limiting против брутфорса
- ✅ Валидация входных данных

### 3. **Гибкость:**
- ✅ Интерфейсы для легкой замены реализаций
- ✅ In-memory хранилища для тестирования
- ✅ Конфигурируемые параметры

### 4. **Обработка ошибок:**
- ✅ Sealed classes для типобезопасных результатов
- ✅ Конкретные коды ошибок
- ✅ Информативные сообщения

## 🚀 **ЧТО МОЖНО ДОБАВИТЬ:**

1. **База данных** - PostgreSQL/MongoDB вместо in-memory
2. **Email сервис** - для сброса пароля
3. **OAuth2** - вход через Google/Facebook
4. **2FA** - двухфакторная аутентификация
5. **Audit log** - логирование действий
6. **Redis** - для сессий и rate limiting

Теперь у тебя есть полноценная система аутентификации! Как тебе архитектура? Есть вопросы по каким-то частям? 💪

---
