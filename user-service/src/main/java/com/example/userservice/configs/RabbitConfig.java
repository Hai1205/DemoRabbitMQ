package com.example.userservice.configs;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

import com.example.userservice.dtos.UserDto;
import com.example.userservice.dtos.requests.AuthRequest;

@Configuration
public class RabbitConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();

        // Map auth-service types to user-service types
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("com.example.authservice.dtos.requests.AuthRequest", AuthRequest.class);
        idClassMapping.put("com.example.authservice.UserDto", UserDto.class);
        classMapper.setIdClassMapping(idClassMapping);

        // Trust all packages (alternative approach - less secure but simpler)
        classMapper.setTrustedPackages("com.example.*");

        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setReplyTimeout(60000);
        return template;
    }
}
