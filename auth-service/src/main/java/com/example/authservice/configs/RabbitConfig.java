package com.example.authservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import java.util.HashMap;
import java.util.Map;

import com.example.authservice.dtos.UserDto;

import com.example.authservice.dtos.UserDto;

@Configuration
public class RabbitConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();

        // Map user-service types to auth-service types
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("com.example.userservice.dtos.UserDto", UserDto.class);
        classMapper.setIdClassMapping(idClassMapping);

        // Trust all packages (alternative approach - less secure but simpler)
        classMapper.setTrustedPackages("*");

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
