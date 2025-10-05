package tv.codealong.tutorials.various.sealedclass.sealedclassesexamples

//3. UI State Management (очень популярный кейс)
//
// ✅ Идеально для состояния UI
sealed class ScreenState<out T> {
    data object Loading : ScreenState<Nothing>()
    data class Success<T>(val data: T) : ScreenState<T>()
    data class Error(val message: String, val retryAction: () -> Unit) : ScreenState<Nothing>()
    data object Empty : ScreenState<Nothing>()

    // Полезные методы
    fun isLoading(): Boolean = this is Loading
    fun getDataOrNull(): T? = (this as? Success)?.data
    fun getErrorOrNull(): String? = (this as? Error)?.message

    // Transform метод
    fun <R> map(transform: (T) -> R): ScreenState<R> = when (this) {
        is Loading -> Loading
        is Success -> Success(transform(data))
        is Error -> Error(message, retryAction)
        is Empty -> Empty
    }
}

// Использование в Android/Compose
//fun UserProfileScreen(state: ScreenState<User>) {
//    when (state) {
//        is ScreenState.Loading -> ShowSpinner()
//        is ScreenState.Success -> ShowUserProfile(user = state.data)
//        is ScreenState.Error -> ShowError(
//            message = state.message,
//            onRetry = state.retryAction
//        )
//        is ScreenState.Empty -> ShowEmptyState()
//    }
//}