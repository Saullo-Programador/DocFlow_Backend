package com.example.DocFlowBackend.service;

import com.example.DocFlowBackend.dto.LoginRequestDTO;
import com.example.DocFlowBackend.dto.RegisterRequestDTO;
import com.example.DocFlowBackend.entity.User;
import com.example.DocFlowBackend.exception.GlobalExceptionHandler;
import com.example.DocFlowBackend.repository.UserRepository;
import com.example.DocFlowBackend.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       JwtService jwtService,
                       BCryptPasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public String login(LoginRequestDTO request){

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new GlobalExceptionHandler.InvalidCredentialsException("Email ou senha inválidos"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new GlobalExceptionHandler.InvalidCredentialsException("Email ou senha inválidos");
        }

        // Agora usando ID no token
        return jwtService.generateToken(user.getId().toString());
    }

    public String register(RegisterRequestDTO request){

        // validação básica
        if(request.getEmail() == null || request.getEmail().isBlank()){
            throw new GlobalExceptionHandler.InvalidCredentialsException("Email inválido");
        }

        if(request.getPassword() == null || request.getPassword().length() < 6){
            throw new GlobalExceptionHandler.InvalidCredentialsException("Senha deve ter pelo menos 6 caracteres");
        }

        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new GlobalExceptionHandler.UserAlreadyExistsException("Usuário já existente");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return jwtService.generateToken(user.getId().toString());
    }


}