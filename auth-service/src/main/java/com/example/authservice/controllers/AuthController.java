package com.example.authservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import java.util.concurrent.CompletableFuture;

import com.example.authservice.dtos.requests.AuthRequest;
import com.example.authservice.dtos.UserDto;
import com.example.authservice.services.producers.AuthProducer;

@RestController
public class AuthController {
    private final AuthProducer authProducer;

    public AuthController(AuthProducer authProducer) {
        this.authProducer = authProducer;
    }

    @PostMapping("/login")
    public DeferredResult<ResponseEntity<?>> login(@RequestBody AuthRequest req) {
        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();
        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("[AuthService] Received login request for: " + req.getEmail());
                Object resp = authProducer.authenticate(req);
                System.out.println("[AuthService] Authentication response: " + resp);
                if (resp == null) {
                    System.out.println("[AuthService] Authentication failed - null response");
                    result.setResult(ResponseEntity.status(401).body("Invalid credentials"));
                } else {
                    UserDto userDto = (UserDto) resp;
                    System.out.println("[AuthService] User logged in: " + userDto.getEmail());
                    authProducer.publishLoginSuccess(userDto);
                    result.setResult(ResponseEntity.ok(resp));
                }
            } catch (Exception e) {
                System.err.println("[AuthService] Error during authentication: " + e.getMessage());
                e.printStackTrace();
                result.setErrorResult(ResponseEntity.status(500).body("Internal server error"));
            }
        });
        return result;
    }
}
