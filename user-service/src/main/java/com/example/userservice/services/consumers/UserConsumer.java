package com.example.userservice.services.consumers;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.util.Map;

import com.example.userservice.dtos.RPCResponse;

@Component
public class UserConsumer {

    private final Map<String, String> users = Map.of(
            "hai@example.com", "password123",
            "alice@example.com", "alicepwd");

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "auth.user.user-service.queue", durable = "true"), exchange = @Exchange(value = "auth.user.exchange", type = "topic"), key = "auth.user.authenticate.request"))
    public RPCResponse handleAuth(@Payload Map<String, Object> req) {
        try {
            Thread.sleep(15000); // 15 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[UserService] Interrupted while waiting");
            return new RPCResponse(500, "Internal server error", null);
        }

        String email = (String) req.get("email");
        String password = (String) req.get("password");

        System.out.println("[UserService] Received auth request for: " + email);

        String pwd = users.get(email);
        if (pwd != null && pwd.equals(password)) {
            Map<String, Object> userData = Map.of("id", 1L, "email", email, "fullName", "Demo User");
            return new RPCResponse(200, "Authentication successful", userData);
        }

        System.out.println("[UserService] auth failed for: " + email);
        return new RPCResponse(401, "Invalid credentials", null);
    }
}
