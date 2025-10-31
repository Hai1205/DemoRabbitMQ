package com.example.authservice.services.producers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthProducer {
    private final RabbitTemplate rabbitTemplate;

    public AuthProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public Object authenticate(Map<String, Object> req) {
        return rabbitTemplate.convertSendAndReceive("auth.user.exchange",
                "auth.user.authenticate.request", req);
    }

    public void publishLoginSuccess(Map<String, Object> user) {
        rabbitTemplate.convertAndSend("auth.authenticate.exchange", "auth.authenticate.login.success", user);
    }
}