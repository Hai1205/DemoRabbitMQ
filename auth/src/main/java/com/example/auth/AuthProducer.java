package com.example.auth;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthProducer {
    private final RabbitTemplate rabbitTemplate;

    public AuthProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public Object authenticate(AuthRequest req) {
        return rabbitTemplate.convertSendAndReceive("auth.user.exchange", "auth.user.authenticate.request", req);
    }

    public void publishLoginSuccess(UserDto user) {
        rabbitTemplate.convertAndSend("auth.authenticate.exchange", "auth.authenticate.login.success", user);
    }
}