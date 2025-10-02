package tv.codealong.tutorials.various.sealedclassvssealedinterface

//наследники должны быть в том же пакете, иначе ошибка
class UnknownState(val code: Int) : LimitCheckResult {
    override val isApproved: Boolean
        get() = TODO("Not yet implemented")
}