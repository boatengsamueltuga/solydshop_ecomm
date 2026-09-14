package com.solydshop.ecommerce.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This service only publishes notification-created events, so it only needs
 * to declare the exchange it publishes to - declaring the queue/binding is
 * the consuming service's responsibility (see solydshop-notifications).
 */
@Configuration
public class RabbitMQConfig {

    @Value("${notifications.rabbitmq.exchange}")
    private String exchangeName;

    @Bean
    public DirectExchange notificationsExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
