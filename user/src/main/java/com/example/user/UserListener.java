package com.example.user;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class UserListener {

    // Hard-coded demo users
    private final Map<String, String> users = Map.of(
            "hai@example.com", "password123",
            "alice@example.com", "alicepwd");

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "auth.user.user-service.queue", durable = "true"), exchange = @Exchange(value = "auth.user.exchange", type = "topic"), key = "auth.user.authenticate.request"))
    public UserDto handleAuth(@Payload AuthRequest req) {
        try {
            Thread.sleep(15000); // 15 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[UserService] Interrupted while waiting");
            return null;
        }

        System.out.println("[UserService] Received auth request for: " + req.getEmail());

        String pwd = users.get(req.getEmail());
        if (pwd != null && pwd.equals(req.getPassword())) {
            return new UserDto(1L, req.getEmail(), "Demo User");
        }

        System.out.println("[UserService] auth failed for: " + req.getEmail());
        return null;
    }
}
