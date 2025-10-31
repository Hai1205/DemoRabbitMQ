package com.example.authservice.services.producers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.authservice.dtos.requests.AuthRequest;
import com.example.authservice.dtos.UserDto;

@Component
public class AuthProducer {
    private final RabbitTemplate rabbitTemplate;

    public AuthProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public Object authenticate(AuthRequest req) {
        // RabbitHeader header = RabbitHeader.builder()
        // .correlationId(UUID.randomUUID().toString())
        // // .replyTo(RabbitConstants.AUTH_REPLY_QUEUE)
        // // .replyExchange(RabbitConstants.AUTH_EXCHANGE)
        // .timestamp(System.currentTimeMillis())
        // .sourceService(RabbitConstants.AUTH_SERVICE)
        // .targetService(RabbitConstants.USER_SERVICE)
        // .build();

        // Map<String, Object> payload = new HashMap<>();
        // payload.put("email", email);
        // payload.put("password", password);

        // var wrapper = Map.of("header", header, "payload", payload);
        // String json = objectMapper.writeValueAsString(wrapper);
        // Message message = new Message(json.getBytes(), new MessageProperties());

        // rabbitTemplate.send(exchange, routingKey, message);

        // Object response = rabbitTemplate.receiveAndConvert(header.getReplyTo(),
        // 8000);
        // if (response == null)
        // throw new RuntimeException("Timeout waiting for response");

        return rabbitTemplate.convertSendAndReceive("auth.user.exchange",
                "auth.user.authenticate.request", req);
    }

    public void publishLoginSuccess(UserDto user) {
        rabbitTemplate.convertAndSend("auth.authenticate.exchange", "auth.authenticate.login.success", user);
    }
}