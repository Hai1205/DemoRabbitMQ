package com.example.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import java.util.concurrent.CompletableFuture;

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
                Object resp = authProducer.authenticate(req);
                if (resp == null) {
                    result.setResult(ResponseEntity.status(401).body("Invalid credentials"));
                } else {
                    System.out.println("[AuthService] User logged in: " + ((UserDto) resp).getEmail());
                    authProducer.publishLoginSuccess((UserDto) resp);
                    result.setResult(ResponseEntity.ok(resp));
                }
            } catch (Exception e) {
                result.setErrorResult(ResponseEntity.status(500).body("Internal server error"));
            }
        });
        return result;
    }
}
