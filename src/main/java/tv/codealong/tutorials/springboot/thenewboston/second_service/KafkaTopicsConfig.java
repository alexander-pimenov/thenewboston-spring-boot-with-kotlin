package tv.codealong.tutorials.springboot.thenewboston.second_service;


import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * KafkaTopicsConfig для объявления топиков
 */
@Configuration
public class KafkaTopicsConfig {

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.topics.email:email-events}")
    private String emailTopic;

    @Value("${spring.kafka.topics.notification:notification-events}")
    private String notificationTopic;

    @Value("${spring.kafka.topics.user:user-events}")
    private String userTopic;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic emailTopic() {
        return new NewTopic(emailTopic, 3, (short) 1); // 3 partitions, replication factor 1
    }

    @Bean
    public NewTopic notificationTopic() {
        return new NewTopic(notificationTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic userTopic() {
        return new NewTopic(userTopic, 3, (short) 1);
    }
}
