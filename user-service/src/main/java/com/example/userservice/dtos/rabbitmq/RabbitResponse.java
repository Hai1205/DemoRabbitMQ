package com.example.userservice.dtos.rabbitmq;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RabbitResponse<T> {
    private int code;
    private String message;
    private T data;
}