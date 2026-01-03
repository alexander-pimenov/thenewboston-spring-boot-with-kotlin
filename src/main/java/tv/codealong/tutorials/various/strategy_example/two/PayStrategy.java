package tv.codealong.tutorials.various.strategy_example.two;

/**
 * Общий интерфейс всех стратегий.
 * Общий интерфейс стратегий оплаты.
 */
public interface PayStrategy {
    boolean pay(int paymentAmount);

    void collectPaymentDetails();
}
