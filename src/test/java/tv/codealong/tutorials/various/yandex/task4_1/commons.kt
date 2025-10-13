package tv.codealong.tutorials.various.yandex.task4_1

import java.security.SecureRandom
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * 🎯 КЛЮЧЕВЫЕ АРХИТЕКТУРНЫЕ РЕШЕНИЯ:
 * 1. Разделение ответственности:
 * - AuthenticationService - бизнес-логика
 * - UserRepository / SessionRepository - хранение данных
 * - PasswordEncoder / TokenProvider - инфраструктура
 * - AuthController - web слой
 *
 * 2. Безопасность:
 * ✅ Хеширование паролей (никогда не храним в открытом виде)
 * ✅ JWT токены с expiration
 * ✅ Rate limiting против брутфорса
 * ✅ Валидация входных данных
 *
 * 3. Гибкость:
 * ✅ Интерфейсы для легкой замены реализаций
 * ✅ In-memory хранилища для тестирования
 * ✅ Конфигурируемые параметры
 *
 * 4. Обработка ошибок:
 * ✅ Sealed classes для типобезопасных результатов
 * ✅ Конкретные коды ошибок
 * ✅ Информативные сообщения
 *
 * 🚀 ЧТО МОЖНО ДОБАВИТЬ:
 * - База данных - PostgreSQL/MongoDB вместо in-memory
 * - Email сервис - для сброса пароля
 * - OAuth2 - вход через Google/Facebook
 * - 2FA - двухфакторная аутентификация
 * - Audit log - логирование действий
 * - Redis - для сессий и rate limiting
 */
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
    val user: UserInfo,
)

data class UserInfo(
    val id: String,
    val email: String,
    val roles: Set<UserRole>,
    val isActive: Boolean,
)

// 🎯 3. КОНФИГУРАЦИЯ
data class AuthConfig(
    val jwtSecret: String,
    val accessTokenExpiration: java.time.Duration = java.time.Duration.ofHours(2),
    val refreshTokenExpiration: java.time.Duration = java.time.Duration.ofDays(30),
    val passwordResetTokenExpiration: java.time.Duration = java.time.Duration.ofHours(1),
    val maxLoginAttempts: Int = 5,
    val loginBlockDuration: java.time.Duration = java.time.Duration.ofMinutes(30),
    val bcryptRounds: Int = 12,
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
            val existingUser =
                usersById[user.id] ?: return AuthResult.Error("User not found", AuthErrorCode.USER_NOT_FOUND)

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
            val user = usersById[id] ?: return AuthResult.Error("User not found", AuthErrorCode.USER_NOT_FOUND)
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
    private val rateLimiter: RateLimiter = SimpleRateLimiter(),
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
            ?: return AuthResult.Error("User not found", AuthErrorCode.USER_NOT_FOUND)
                .also { failedLogins.incrementAndGet() }

        // 3. Проверка активности
        if (!user.isActive) {
            return AuthResult.Error("User account is inactive", AuthErrorCode.USER_INACTIVE)
                .also { failedLogins.incrementAndGet() }
        }

        // 4. Проверка пароля
        if (!passwordEncoder.matches(password, user.passwordHash)) {
            return AuthResult.Error("Invalid password", AuthErrorCode.INVALID_PASSWORD)
                .also { failedLogins.incrementAndGet() }
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
/**
 * ## 🎯 **ПОШАГОВЫЙ РАЗБОР ВЫПОЛНЕНИЯ:**
 * Для лучшего понимания функции getOrPut
 * ### Сценарий: **Первый вызов для нового пользователя**
 * ```kotlin
 * val limiter = SimpleRateLimiter()
 *
 * // Первый вызов для "user123":
 * limiter.tryAcquire("user123")
 *
 * // Внутри getOrPut:
 * // 1. attempts["user123"] = null (ключа нет)
 * // 2. Выполняется lambda { mutableListOf() } → создаётся пустой список
 * // 3. attempts["user123"] = mutableListOf() // добавляется в Map
 * // 4. Возвращается новый пустой список
 * ```
 *
 * ### Сценарий: **Повторный вызов для того же пользователя**
 * ```kotlin
 * // Второй вызов для "user123":
 * limiter.tryAcquire("user123")
 *
 * // Внутри getOrPut:
 * // 1. attempts["user123"] = [Instant1] (ключ уже есть)
 * // 2. Lambda НЕ выполняется!
 * // 3. Возвращается существующий список [Instant1]
 * ```
 *
 * ## 📊 **ВИЗУАЛИЗАЦИЯ ПРОЦЕССА:**
 *
 * ```
 * ПЕРВЫЙ ВЫЗОВ tryAcquire("user123"):
 *
 * attempts Map: {}
 * ↓ getOrPut("user123") { mutableListOf() }
 * attempts Map: {"user123" → []}
 * Возвращает: []
 *
 * ПОВТОРНЫЙ ВЫЗОВ tryAcquire("user123"):
 *
 * attempts Map: {"user123" → [timestamp1]}
 * ↓ getOrPut("user123") { mutableListOf() } // lambda НЕ выполняется!
 * attempts Map: {"user123" → [timestamp1, timestamp2]}
 * Возвращает: [timestamp1, timestamp2]
 * ```
 */
interface RateLimiter {
    fun tryAcquire(key: String): Boolean
}

class SimpleRateLimiter : RateLimiter {
    private val attempts = ConcurrentHashMap<String, MutableList<Instant>>()

    override fun tryAcquire(key: String): Boolean {
        val now = Instant.now()
        val windowStart = now.minusSeconds(60) // 1 minute window

        //🎯 ПОЧЕМУ МЫ ВЫБРАЛИ synchronized(attempts):
        // - Простота - легко понять что защищаем
        // - Ясность - видна связь между защищаемым ресурсом и синхронизацией
        // - ✅ ХОРОШО: synchronized на том, что защищаем
        // - Достаточность - защищает именно то, что нужно
        //- Идиоматичность - распространённый паттерн в Java/Kotlin
        synchronized(attempts) {
            // В этом блоке только ОДИН поток может работать с attempts
            // 🔍 Вот эта строка:
            val keyAttempts = attempts.getOrPut(key) { mutableListOf() }
            // ↑ Гарантирует что для каждого ключа всегда есть список
            // ↑ Не важно новый пользователь или существующий

            // Что происходит:
            // 1. Пытаемся получить список попыток для ключа (например, "login:alice@email.com")
            // 2. Если такого ключа нет → создаём пустой mutableListOf() и добавляем в Map
            // 3. Если ключ есть → просто возвращаем существующий список

            // Эквивалент без getOrPut:
            // var keyAttempts = attempts[key]
            // if (keyAttempts == null) {
            //     keyAttempts = mutableListOf()
            //     attempts[key] = keyAttempts
            // }

            // Дальше можно безопасно работать с keyAttempts
            // Удаляем старые попытки
            keyAttempts.removeAll { it.isBefore(windowStart) }

            // Проверяем лимит (максимум 5 попыток в минуту)
            if (keyAttempts.size >= 5) {
                return false
            }

            keyAttempts.add(now)
            return true
        }
        // 🔓 Монитор автоматически освобождается при выходе из блока
    }
}

// 📊 Метрики
data class AuthMetrics(
    val totalUsers: Long,
    val activeSessions: Long,
    val loginAttempts: Long,
    val successfulLogins: Long,
    val failedLogins: Long,
) {
    val successRate: Double
        get() =
            if (loginAttempts > 0) successfulLogins.toDouble() / loginAttempts.toDouble() else 0.0
}

// ✅ data class для пользователя
data class User(
    val id: String,
    val email: String,
    val passwordHash: String, // Никогда не храним пароль в открытом виде!
    val isActive: Boolean = true,
    val roles: Set<UserRole> = setOf(UserRole.USER),
    val createdAt: Instant = Instant.now(),
    val lastLoginAt: Instant? = null,
)

enum class UserRole {
    USER, ADMIN, MODERATOR
}

// ✅ data class для сессии
data class UserSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val deviceInfo: DeviceInfo? = null,
    val createdAt: Instant = Instant.now(),
    val expiresAt: Instant,
    val isRevoked: Boolean = false,
)

data class DeviceInfo(
    val userAgent: String,
    val ipAddress: String,
    val deviceType: String = "unknown",
)

// ✅ data class для токена сброса
data class PasswordResetToken(
    val token: String,
    val userId: String,
    val createdAt: Instant = Instant.now(),
    val expiresAt: Instant,
    val isUsed: Boolean = false,
)


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
            val session = sessionsByToken[token]
                ?: return AuthResult.Error("Session not found", AuthErrorCode.INVALID_TOKEN)

            sessionsByToken.remove(token)
            sessionsByUserId[session.userId]?.remove(session)

            return AuthResult.Success(Unit)
        }
    }

    override fun revokeAllUserSessions(userId: String): AuthResult<Unit> {
        synchronized(this) {
            val userSessions = sessionsByUserId[userId]
                ?: return AuthResult.Success(Unit)

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


// 🎯 12. ИНТЕГРАЦИЯ С WEB ФРЕЙМВОРКОМ (пример)

class AuthController(
    private val authService: AuthenticationService,
) {

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
        val token = extractToken(authHeader)
            ?: return ApiResponse.error("Missing token")

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
    val error: String? = null,
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(true, data)
        fun <T> error(message: String): ApiResponse<T> = ApiResponse(false, error = message)
    }
}

