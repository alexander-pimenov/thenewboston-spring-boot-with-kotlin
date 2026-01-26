package tv.codealong.tutorials.springboot.thenewboston.service.java;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import tv.codealong.tutorials.springboot.thenewboston.utils.db.java.AbstractServiceWithTransactionImpl;

/**
 * Пример использования в Java мощной утилиты для работы с транзакциями {@link AbstractServiceWithTransactionImpl}
 */
@Slf4j
@Service
public class PaymentService extends AbstractServiceWithTransactionImpl {

    protected PaymentService(PlatformTransactionManager transactionManager) {
        super(transactionManager);
    }

    public void processPayment(String userId) {
        doInSeparatedTransactionWithoutResult(status -> {
            // Это выполнится в новой транзакции
            log.info("Создаём платёж для {}", userId);
            // ... сохранение в БД
        });
    }

    @Override
    protected Runnable beforeTransaction() {
        return () -> log.info("Транзакция начата");
    }

    @Override
    protected Runnable afterTransaction() {
        return () -> log.info("Транзакция завершена");
    }
}
