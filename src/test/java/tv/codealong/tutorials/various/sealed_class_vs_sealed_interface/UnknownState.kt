package tv.codealong.tutorials.various.sealed_class_vs_sealed_interface

//наследники должны быть в том же пакете, иначе ошибка
class UnknownState(val code: Int) : LimitCheckResult {
    override val isApproved: Boolean
        get() = TODO("Not yet implemented")
}