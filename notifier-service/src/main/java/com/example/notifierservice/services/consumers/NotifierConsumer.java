package com.example.notifierservice.services.consumers;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.*;
import java.util.Map;

@Component
public class NotifierConsumer {

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "auth.authenticate.notifier-service.queue", durable = "true"), exchange = @Exchange(value = "auth.authenticate.exchange", type = "topic"), key = "auth.authenticate.#"))
    public void handle(@Payload Map<String, Object> user) {
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[Notifier] Interrupted while waiting");
            return;
        }

        System.out.println("[Notifier] Congratulation: " + user.get("email"));
    }
}
