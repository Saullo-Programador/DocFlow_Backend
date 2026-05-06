package com.example.DocFlowBackend.controller;

import com.example.DocFlowBackend.dto.AuthResponseDTO;
import com.example.DocFlowBackend.dto.LoginRequestDTO;
import com.example.DocFlowBackend.dto.RefreshTokenRequestDTO;
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

    @GetMapping("/")
    public String start(){
        return "Backend Rodando ";
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @RequestBody LoginRequestDTO request
    ){
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @RequestBody @Valid RegisterRequestDTO request
    ){
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(
            @RequestBody RefreshTokenRequestDTO request
    ) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

}
