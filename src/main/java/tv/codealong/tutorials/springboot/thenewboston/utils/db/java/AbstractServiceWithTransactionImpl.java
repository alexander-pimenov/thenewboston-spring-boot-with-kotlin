package tv.codealong.tutorials.springboot.thenewboston.utils.db.java;


import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Абстрактный сервис для безопасной работы с транзакциями в Spring.
 * Запрещает выполнение в "опасных" потоках (например, ForkJoinPool).
 */
public abstract class AbstractServiceWithTransactionImpl {

    private final PlatformTransactionManager transactionManager;

    // Запрещённые имена потоков для транзакций
    private static final Set<String> ILLEGAL_THREAD_NAMES = Set.of("ForkJoinPool", "coroutine");

    protected AbstractServiceWithTransactionImpl(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Возвращает TransactionTemplate с заданным уровнем распространения транзакции.
     */
    protected TransactionTemplate transactionTemplate(int propagationLevel) {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.setPropagationBehavior(propagationLevel);
        return tt;
    }

    /**
     * Хук: выполняется перед транзакцией. Можно переопределить.
     *
     * @return Runnable, который будет выполнен, или null.
     */
    protected Runnable beforeTransaction() {
        return null;
    }

    /**
     * Хук: выполняется после транзакции. Можно переопределить.
     *
     * @return Runnable, который будет выполнен, или null.
     */
    protected Runnable afterTransaction() {
        return null;
    }

    /**
     * Проверяет, что текущий поток разрешён для транзакций.
     */
    protected void assertTransactionAllowed() {
        String threadName = Thread.currentThread().getName();
        for (String illegalName : ILLEGAL_THREAD_NAMES) {
            Assert.doesNotContain(threadName, illegalName,
                    () -> "Illegal threadName=" + threadName + " to run transaction");
        }
    }

    /**
     * Выполняет блок кода в отдельной транзакции (REQUIRES_NEW), даже если уже есть активная.
     * Поддерживает хуки before/after.
     */
    protected void doInSeparatedTransactionWithoutResult(Consumer<TransactionStatus> block) {
        assertTransactionAllowed();
        TransactionTemplate outerTt = transactionTemplate(TransactionDefinition.PROPAGATION_NEVER);
        outerTt.executeWithoutResult(status -> {
            TransactionTemplate innerTt = transactionTemplate(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            innerTt.executeWithoutResult(innerStatus -> {
                Runnable before = beforeTransaction();
                if (before != null) before.run();
                try {
                    block.accept(innerStatus);
                } finally {
                    Runnable after = afterTransaction();
                    if (after != null) after.run();
                }
            });
        });
    }

    /**
     * Выполняет callback в отдельной транзакции (REQUIRES_NEW).
     */
    protected <T> T doInSeparatedTransaction(TransactionCallback<T> callback) {
        assertTransactionAllowed();
        TransactionTemplate outerTt = transactionTemplate(TransactionDefinition.PROPAGATION_NEVER);
        return outerTt.execute(status -> {
            TransactionTemplate innerTt = transactionTemplate(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            return doInTransaction(innerTt, callback);
        });
    }

    /**
     * Выполняет блок в обязательной внешней транзакции (MANDATORY).
     */
    protected void doInOuterMandatoryTransactionWithoutResult(Consumer<TransactionStatus> block) {
        assertTransactionAllowed();
        TransactionTemplate tt = transactionTemplate(TransactionDefinition.PROPAGATION_MANDATORY);
        tt.executeWithoutResult(status -> {
            Runnable before = beforeTransaction();
            if (before != null) before.run();
            try {
                block.accept(status);
            } finally {
                Runnable after = afterTransaction();
                if (after != null) after.run();
            }
        });
    }

    /**
     * Выполняет callback в обязательной внешней транзакции (MANDATORY).
     */
    protected <T> T doInOuterMandatoryTransaction(TransactionCallback<T> callback) {
        assertTransactionAllowed();
        return doInTransaction(transactionTemplate(TransactionDefinition.PROPAGATION_MANDATORY), callback);
    }

    /**
     * Вспомогательный метод: выполняет callback в транзакции с хуками.
     */
    private <T> T doInTransaction(TransactionTemplate tt, TransactionCallback<T> callback) {
        Runnable before = beforeTransaction();
        if (before != null) before.run();
        try {
            return tt.execute(callback);
        } finally {
            Runnable after = afterTransaction();
            if (after != null) after.run();
        }
    }
}
