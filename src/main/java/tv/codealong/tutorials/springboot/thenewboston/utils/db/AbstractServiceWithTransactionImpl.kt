package tv.codealong.tutorials.springboot.thenewboston.utils.db

import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.util.Assert
import java.util.function.Consumer

fun PlatformTransactionManager.withPropagation(levelPropagation: Int): TransactionTemplate = run {
    val tt = TransactionTemplate(this)
    tt.propagationBehavior = levelPropagation
    tt
}

/**
 * Абстрактный сервис для безопасной работы с транзакциями в Spring.
 * Запрещает выполнение в "опасных" потоках (например, ForkJoinPool).
 */
abstract class AbstractServiceWithTransactionImpl(
    private val transactionManager: PlatformTransactionManager
) {

    companion object {
        val illegalThreadNames = setOf("ForkJoinPool", "coroutine")
    }

    protected val transactionTemplate: (Int) -> TransactionTemplate = { levelPropagation: Int ->
        transactionManager.withPropagation(levelPropagation)
    }

    protected open fun beforeTransaction(): Runnable? {
        return null
    }

    protected open fun afterTransaction(): Runnable? {
        return null
    }

    protected fun assertTransactionAllowed() {
        val threadName = Thread.currentThread().name
        illegalThreadNames.forEach {
            Assert.doesNotContain(threadName, it) {
                "Illegal threadName=$threadName to run transaction"
            }
        }
    }

    protected open fun doInSeparatedTransactionWithoutResult(block: Consumer<TransactionStatus?>) {
        assertTransactionAllowed()
        val tt = transactionTemplate(TransactionDefinition.PROPAGATION_NEVER)
        tt.execute(object : TransactionCallbackWithoutResult() {
            override fun doInTransactionWithoutResult(transactionStatus: TransactionStatus) {
                tt.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
                tt.execute(object : TransactionCallbackWithoutResult() {
                    override fun doInTransactionWithoutResult(status: TransactionStatus) {
                        try {
                            beforeTransaction()?.run()
                            block.accept(status)
                        } finally {
                            afterTransaction()?.run()
                        }
                    }
                })
            }
        })
    }

    protected open fun <T> doInSeparatedTransaction(callback: TransactionCallback<T>): T? {
        assertTransactionAllowed()
        return transactionTemplate(TransactionDefinition.PROPAGATION_NEVER)
            .let { tt ->
                tt.execute {
                    tt.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
                    doInTransaction(tt, callback)
                }
            }
    }

    protected open fun doInOuterMandatoryTransactionWithoutResult(block: Consumer<TransactionStatus?>) {
        assertTransactionAllowed()
        val tt = transactionTemplate(TransactionDefinition.PROPAGATION_MANDATORY)
        tt.execute(object : TransactionCallbackWithoutResult() {
            override fun doInTransactionWithoutResult(transactionStatus: TransactionStatus) {
                beforeTransaction()?.run()
                try {
                    block.accept(transactionStatus)
                } finally {
                    afterTransaction()?.run()
                }
            }
        })
    }


    protected open fun <T> doInOuterMandatoryTransaction(callback: TransactionCallback<T>): T? {
        assertTransactionAllowed()
        return doInTransaction(transactionTemplate(TransactionDefinition.PROPAGATION_MANDATORY), callback)
    }

    private fun <T> doInTransaction(
        tt: TransactionTemplate,
        callback: TransactionCallback<T>
    ): T? {
        beforeTransaction()
        return try {
            tt.execute(callback)
        } finally {
            afterTransaction()
        }
    }
}