package tv.codealong.tutorials.various.yandex.test6_3_real;


import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Пример использования.
 * <p>
 * Ключевые моменты реализации:
 * Принципы SOLID:
 * <p>
 * - DiscountRepository - интерфейс для инверсии зависимостей
 * - Каждый класс имеет одну ответственность
 * - Легко расширять функциональность
 * <p>
 * Иммутабельность:
 * - Модели данных иммутабельны где это возможно
 * - Создаются новые объекты при применении скидок
 * <p>
 * Точность вычислений:
 * - Используется BigDecimal для финансовых расчетов
 * - Округление до 2 знаков после запятой
 * <p>
 * Тестируемость:
 * - Используется Mockito для мокирования зависимостей
 * - Параметризованные тесты
 * - Покрыты граничные случаи
 * <p>
 * Безопасность:
 * - Валидация входных данных
 * - Проверка соответствия customerId
 * <p>
 * Это решение демонстрирует хорошие практики разработки и может быть легко расширено (например, добавив разные типы скидок, историю применения и т.д.).
 */
public class MainExample {
    public static void main(String[] args) {
        // Создаем репозиторий со скидками
        DiscountRepository discountRepository = new InMemoryDiscountRepository();

        // Создаем сервис лояльности
        LoyaltyService loyaltyService = new LoyaltyService(discountRepository);

        // Создаем идентификатор покупателя
        String customerId = "vip-customer";
        // Создаем покупки для покупателя
        Purchase laptop = new Purchase("laptop-123", BigDecimal.valueOf(1500));
        Purchase mouse = new Purchase("mouse-456", BigDecimal.valueOf(50));
        Purchase keyboard = new Purchase("keyboard-789", BigDecimal.valueOf(100));

        // Создаем корзину для покупателя
        ShoppingCart cart = new ShoppingCart(customerId,
                Arrays.asList(laptop, mouse, keyboard));

        System.out.println("Оригинальная корзина:");
        System.out.println("Общая стоимость: " + cart.calculateTotalOriginalPrice());

        // Применяем скидку
        ShoppingCart discountedCart = loyaltyService.applyDiscount(customerId, cart);

        System.out.println("\nКорзина со скидкой:");
        for (Purchase purchase : discountedCart.getPurchases()) {
            System.out.printf("Товар: %s, Оригинальная цена: %.2f, Цена со скидкой: %.2f%n",
                    purchase.getProductId(),
                    purchase.getOriginalPrice(),
                    purchase.getFinalPrice());
        }

        System.out.println("Общая стоимость со скидкой: " + discountedCart.calculateTotalFinalPrice());

        // Рассчитываем общую скидку
        BigDecimal totalDiscount = loyaltyService.calculateTotalDiscount(customerId, cart);
        System.out.println("Общая скидка: " + totalDiscount);
    }
}
