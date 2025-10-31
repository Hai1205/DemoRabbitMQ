package com.example.mailerservice.services.consumers;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.*;

import com.example.mailerservice.dtos.UserDto;

@Component
public class MailConsumer {

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "auth.authenticate.mailer-service.queue", durable = "true"), exchange = @Exchange(value = "auth.authenticate.exchange", type = "topic"), key = "auth.authenticate.login.*"))
    public void handle(@Payload UserDto userDto) {
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[Notifier] Interrupted while waiting");
            return;
        }

        System.out.println("[Mailer] Congratulation: " + userDto);
    }
}
