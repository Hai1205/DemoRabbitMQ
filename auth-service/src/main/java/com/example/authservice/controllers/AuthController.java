package com.example.authservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import java.util.concurrent.CompletableFuture;
import java.util.Map;

import com.example.authservice.dtos.RPCResponse;
import com.example.authservice.services.producers.AuthProducer;

@RestController
public class AuthController {
    private final AuthProducer authProducer;

    public AuthController(AuthProducer authProducer) {
        this.authProducer = authProducer;
    }

    @PostMapping("/login")
    public DeferredResult<ResponseEntity<RPCResponse>> login(@RequestBody Map<String, Object> req) {
        DeferredResult<ResponseEntity<RPCResponse>> result = new DeferredResult<>();
        CompletableFuture.runAsync(() -> {
            try {
                String email = (String) req.get("email");
                logInfo("Received login request for: " + email);

                Object resp = authProducer.authenticate(req);
                logInfo("Authentication response: " + resp);

                if (resp == null) {
                    handleNullResponse(result);
                    return;
                }

                RPCResponse responseDto = (RPCResponse) resp;
                handleResponse(responseDto, result);

            } catch (Exception e) {
                handleException(e, result);
            }
        });
        return result;
    }

    private void handleNullResponse(DeferredResult<ResponseEntity<RPCResponse>> result) {
        logInfo("Authentication failed - null response");
        result.setResult(ResponseEntity
                .status(401)
                .body(new RPCResponse(401, "Invalid credentials", null)));
    }

    @SuppressWarnings("unchecked")
    private void handleResponse(RPCResponse responseDto, DeferredResult<ResponseEntity<RPCResponse>> result) {
        if (responseDto.getCode() == 200) {
            Map<String, Object> user = (Map<String, Object>) responseDto.getData();
            logInfo("User logged in: " + user.get("email"));
            authProducer.publishLoginSuccess(user);
        }
        result.setResult(ResponseEntity.ok(responseDto));
    }

    private void handleException(Exception e, DeferredResult<ResponseEntity<RPCResponse>> result) {
        logError("Error during authentication: " + e.getMessage(), e);
        result.setErrorResult(ResponseEntity
                .status(500)
                .body(new RPCResponse(500, "Internal server error", null)));
    }

    private void logInfo(String message) {
        System.out.println("[AuthService] " + message);
    }

    private void logError(String message, Exception e) {
        System.err.println("[AuthService] " + message);
        e.printStackTrace();
    }

}
