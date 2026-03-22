package com.example.DocFlowBackend.controller;

import com.example.DocFlowBackend.dto.AuthResponseDTO;
import com.example.DocFlowBackend.dto.LoginRequestDTO;
import com.example.DocFlowBackend.dto.RegisterRequestDTO;
import com.example.DocFlowBackend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @RequestBody LoginRequestDTO request
    ){

         String token = authService.login(request);

        return ResponseEntity.ok(new AuthResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @RequestBody @Valid RegisterRequestDTO request
    ){
        String token = authService.register(request);

        return ResponseEntity.ok(new AuthResponseDTO(token));

    }
}