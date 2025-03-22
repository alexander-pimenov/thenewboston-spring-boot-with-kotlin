package tv.codealong.tutorials.springboot.thenewboston.strategyexaple.two;

/**
 * Общий интерфейс всех стратегий.
 * Общий интерфейс стратегий оплаты.
 */
public interface PayStrategy {
    boolean pay(int paymentAmount);

    void collectPaymentDetails();
}
