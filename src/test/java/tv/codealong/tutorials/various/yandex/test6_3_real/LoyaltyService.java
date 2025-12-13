package tv.codealong.tutorials.various.yandex.test6_3_real;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Вы — backend-разработчик в интернет-магазине.
 * Дела идут в гору и магазин решил повысить лояльность покупателей, предоставляя им персональные скидки.
 * К вам обратился product owner с задачей создать простую систему лояльности,
 * которая предоставляет процентную скидку на корзину.
 * Размер скидки зависит от покупателя.
 * Аналитики уже определили, какие скидки должны быть предоставлены покупателям.
 * <p>
 * <p>
 * ## Определения
 * <p>
 * Корзина - список покупок покупателя.
 * <p>
 * Покупка:
 * - id товара
 * - цена
 * - итоговая стоимость c учетом скидки
 * <p>
 * Скидка. Для покупателя может быть задан % скидки (целое число).
 * <p>
 * <p>
 * ## Задача
 * Написать часть новой системы лояльности, которая:
 * - на вход получает id покупателя и корзину
 * - вычисляет и применяет скидки
 * - возвращает корзину, в которой учтены скидки. Скидка учитывается в стоимости покупки
 * <p>
 * <p>
 * <strong>Основной сервис лояльности</strong>
 */
public class LoyaltyService {

    private final DiscountRepository discountRepository;

    // Сервис тоже иммутабельный - поле final
    public LoyaltyService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    /**
     * Применяет скидку к корзине покупателя.
     * Соблюдаем иммутабельность - Метод возвращает новый объект, ничего не меняя.
     *
     * @param customerId   ID покупателя
     * @param shoppingCart Корзина покупок
     * @return Корзина с примененными скидками
     */
    public ShoppingCart applyDiscount(String customerId, ShoppingCart shoppingCart) {
        // валидация
        if (!customerId.equals(shoppingCart.getCustomerId())) {
            throw new IllegalArgumentException("Customer ID does not match shopping cart customer ID. Provided: customerId=" + customerId);
        }

        Discount discount = discountRepository.findDiscountByCustomerId(customerId)
                .orElse(new Discount(customerId, 0)); // Если скидки нет, применяем 0%

        // ВАЖНО: создаем новый список с НОВЫМИ объектами Purchase
        List<Purchase> discountedPurchases = shoppingCart.getPurchases().stream()
                .map(purchase -> applyDiscountToPurchase(purchase, discount))
                .collect(Collectors.toList());

        // Возвращаем НОВЫЙ объект ShoppingCart
        return new ShoppingCart(customerId, discountedPurchases);
    }

    private Purchase applyDiscountToPurchase(Purchase originalPurchase, Discount discount) {
        // ВАЖНО: не изменяем исходный объект!
        // Вместо этого создаем новый с примененной скидкой
        Purchase discountedPurchase = new Purchase(
                originalPurchase.getProductId(),
                originalPurchase.getOriginalPrice()
        );

        BigDecimal discountedPrice = discount.applyTo(originalPurchase.getOriginalPrice());
        discountedPurchase.setFinalPrice(discountedPrice);

        return discountedPurchase;
    }

    /**
     * Рассчитывает общую сумму скидки для корзины.
     * (Pure function - зависит только от входных параметров)
     */
    public BigDecimal calculateTotalDiscount(String customerId, ShoppingCart shoppingCart) {
        ShoppingCart discountedCart = applyDiscount(customerId, shoppingCart);
        return shoppingCart.calculateTotalOriginalPrice()
                .subtract(discountedCart.calculateTotalFinalPrice());
    }
}
