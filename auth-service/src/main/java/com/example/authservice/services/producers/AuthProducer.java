package com.example.authservice.services.producers;

import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.authservice.dtos.RPCResponse;

@Component
public class AuthProducer {
    private final RabbitTemplate rabbitTemplate;

    public AuthProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public RPCResponse authenticate(Map<String, Object> req) {
        String exchange = "auth.user.exchange";
        String routingKey = "auth.user.authenticate.request";
        
        return (RPCResponse) rabbitTemplate.convertSendAndReceive(exchange, routingKey, req);
    }

    public void publishLoginSuccess(Map<String, Object> req) {
        String exchange = "auth.authenticate.exchange";
        String routingKey = "auth.authenticate.login.success";

        rabbitTemplate.convertAndSend(exchange, routingKey, req);
    }
}