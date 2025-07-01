package tv.codealong.tutorials.various.flow

class TransferFlowModel(private val id: String) : SberFlowModel {
    override val sfModelId = id
}

class TransactionFlow(private val transferModel: TransferFlowModel, private val transactionType: AuxiliaryFlowMode) : AuxiliaryFlowModel {
    override val sfModel: SberFlowModel = transferModel
    override val mode: AuxiliaryFlowMode = transactionType
}

fun main(args: Array<String>) {
    val transferModel = TransferFlowModel("TXN_12345")
    val defaultTransaction = TransactionFlow(transferModel, AuxiliaryFlowMode.DEFAULT)
    processPayment(defaultTransaction)

    val idempotentTransaction = TransactionFlow(transferModel, AuxiliaryFlowMode.IDEMPOTENT)
    processPayment(idempotentTransaction)

}

/**
 * Функция processPayment() принимает любой объект, реализующий интерфейс AuxiliaryFlowModel,
 * и выбирает поведение исходя из текущего режима.
 */
fun processPayment(auxiliaryFlow: AuxiliaryFlowModel) {
    when (auxiliaryFlow.mode) {
        AuxiliaryFlowMode.DEFAULT -> println("Processing payment in default mode. Auxiliary Flow Model ID: ${auxiliaryFlow.sfModel.sfModelId}")
        AuxiliaryFlowMode.IDEMPOTENT -> println("Processing payment with idempotent behavior. Auxiliary Flow Model ID: ${auxiliaryFlow.sfModel.sfModelId}")
    }
}