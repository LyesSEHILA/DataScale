package com.cyberscale.backend.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "cyberscale.exchange";

    public static final String EXECUTION_QUEUE = "infra.execution";
    public static final String DEPLOY_QUEUE = "infra.deploy";

    public static final String ROUTING_KEY_EXECUTION = "infra.execution";
    public static final String ROUTING_KEY_DEPLOY = "infra.deploy";

    @Value("${app.rabbitmq.queue}")
    private String queueName;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routingkey}")
    private String routingKey;

    @Value("${app.rabbitmq.queue.infra}")
    private String infraQueueName;

    @Value("${app.rabbitmq.routingkey.infra}")
    private String infraRoutingKey;

    @Bean
    public Queue queue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue infraQueue() {
        return new Queue(infraQueueName, true);
    }

    @Bean
    public Queue executionQueue() {
        return new Queue(EXECUTION_QUEUE, true);
    }

    @Bean
    public Queue deployQueue() {
        return new Queue(DEPLOY_QUEUE, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(@Qualifier("queue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    public Binding infraBinding(@Qualifier("infraQueue") Queue infraQueue, TopicExchange exchange) {
        return BindingBuilder.bind(infraQueue).to(exchange).with(infraRoutingKey);
    }

    @Bean
    public Binding executionBinding(@Qualifier("executionQueue") Queue executionQueue, TopicExchange exchange) {
        return BindingBuilder.bind(executionQueue).to(exchange).with(ROUTING_KEY_EXECUTION);
    }

    @Bean
    public Binding deployBinding(@Qualifier("deployQueue") Queue deployQueue, TopicExchange exchange) {
        return BindingBuilder.bind(deployQueue).to(exchange).with(ROUTING_KEY_DEPLOY);
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}