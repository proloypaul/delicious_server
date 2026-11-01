package com.delicious.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${notification.rabbitmq.exchange:delicious.notification.exchange}")
    private String exchangeName;

    @Value("${notification.rabbitmq.dlx-exchange:notification.dlx.exchange}")
    private String dlxExchangeName;

    @Value("${notification.rabbitmq.dlq-queue:notification.dead-letter.queue}")
    private String dlqQueueName;

    @Value("${notification.rabbitmq.queues.seller-order-created:order.seller.notification.queue}")
    private String sellerOrderCreatedQueueName;

    @Value("${notification.rabbitmq.queues.customer-order-dispatched:order.customer.notification.queue}")
    private String customerOrderDispatchedQueueName;

    @Value("${notification.rabbitmq.routing-keys.order-created:order.created}")
    private String orderCreatedRoutingKey;

    @Value("${notification.rabbitmq.routing-keys.order-dispatched:order.dispatched}")
    private String orderDispatchedRoutingKey;

    @Value("${notification.rabbitmq.routing-keys.dlk:notification.dlk}")
    private String dlkRoutingKey;

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxExchangeName, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(dlqQueueName).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(dlkRoutingKey);
    }

    @Bean
    public Queue sellerOrderCreatedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxExchangeName);
        args.put("x-dead-letter-routing-key", dlkRoutingKey);
        return QueueBuilder.durable(sellerOrderCreatedQueueName)
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding sellerOrderCreatedBinding() {
        return BindingBuilder.bind(sellerOrderCreatedQueue())
                .to(notificationExchange())
                .with(orderCreatedRoutingKey);
    }

    @Bean
    public Queue customerOrderDispatchedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxExchangeName);
        args.put("x-dead-letter-routing-key", dlkRoutingKey);
        return QueueBuilder.durable(customerOrderDispatchedQueueName)
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding customerOrderDispatchedBinding() {
        return BindingBuilder.bind(customerOrderDispatchedQueue())
                .to(notificationExchange())
                .with(orderDispatchedRoutingKey);
    }
}
