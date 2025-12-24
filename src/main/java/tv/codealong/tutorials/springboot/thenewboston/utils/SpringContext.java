package tv.codealong.tutorials.springboot.thenewboston.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Прямой доступ к бинам Spring (к Spring IoC).
 * Это класс утилита-обертка над ApplicationContext, которая дает статический доступ к бинам Spring.
 * Главное назначение SpringContext — обход ограничений DI, когда @Autowired недоступен.
 * Утилита для статического доступа к Spring-бинам.
 * <p>
 * Полезна в случаях, когда невозможно использовать {@link org.springframework.beans.factory.annotation.Autowired},
 * например, в статических методах, DTO или классах, созданных вручную.
 * <p>
 * Не используйте в обычных сервисах — предпочтительнее внедрение через конструктор.
 * <p>
 * Статическое состояние — антипаттерн, но иногда оправдан.
 * Это спасательный круг, а не способ внедрения зависимостей по умолчанию.
 */
@Component
public class SpringContext implements ApplicationContextAware {
    private static ApplicationContext context;

    private static final Map<Class<?>, Object> mapBeans = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        SpringContext.context = applicationContext;
    }

    // Только для кэширования. Не регистрирует бин в Spring!
    public static <T> void setCachedBean(Class<T> beanClass, T bean) {
        mapBeans.put(beanClass, bean);
    }

    //кэширует при первом вызове, но не проверяет, изменился ли контекст
    public static <T> T getBean(Class<T> beanClass) {
        assertContextExists();
        if (!mapBeans.containsKey(beanClass)) {
            mapBeans.put(beanClass, context.getBean(beanClass));
        }
        return beanClass.cast(mapBeans.get(beanClass));
    }

    public static <T> T getBean(String bean, Class<T> clazz) {
        assertContextExists();
        return context.getBean(bean, clazz);
    }

    /**
     * getBeanObject(String) — потенциальная уязвимость
     * Принимает строку → преобразует в Class<?> → получает бин.
     * Риск: если beanName приходит из внешнего источника (например, из REST API), это может привести к RCE (если злоумышленник заставит загрузить опасный класс).
     *
     * @param beanName
     * @return
     * @throws ClassNotFoundException
     */
    public static Object getBeanObject(String beanName) throws ClassNotFoundException {
        Class<?> beanClass = ClassUtils.forName(beanName, SpringContext.class.getClassLoader());
        return SpringContext.getBean(beanClass);
    }

    public static Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationClass) {
        assertContextExists();
        return context.getBeansWithAnnotation(annotationClass);
    }

    private static void assertContextExists() {
        if (context == null) {
            throw new IllegalStateException("ApplicationContext is not ready. Is SpringContext bean created? " +
                    "Ensure SpringContext bean is initialized before use.");
        }
    }

    public static boolean containsBean(Class<?> beanClass) {
        return context != null && context.containsBeanDefinition(beanClass.getName()) ||
                mapBeans.containsKey(beanClass);
    }
}
