package com.example.DocFlowBackend.service;

import com.example.DocFlowBackend.dto.AuthResponseDTO;
import com.example.DocFlowBackend.dto.LoginRequestDTO;
import com.example.DocFlowBackend.dto.RegisterRequestDTO;
import com.example.DocFlowBackend.dto.UserResponseDTO;
import com.example.DocFlowBackend.entity.User;
import com.example.DocFlowBackend.exception.GlobalExceptionHandler;
import com.example.DocFlowBackend.mapper.UserMapper;
import com.example.DocFlowBackend.repository.UserRepository;
import com.example.DocFlowBackend.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

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

    public AuthResponseDTO login(LoginRequestDTO request){

        User user = userRepository.findByName(request.getName())
                .orElseThrow(() -> new GlobalExceptionHandler.InvalidCredentialsException("Nome de usuário ou senha inválidos"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new GlobalExceptionHandler.InvalidCredentialsException("Nome de usuário ou senha inválidos");
        }

        String accessToken = jwtService.generateAccessToken(user.getId().toString());
        String refreshToken = jwtService.generateRefreshToken(user.getId().toString());
        UserResponseDTO userDTO = UserMapper.toDTO(user);

        return new AuthResponseDTO(accessToken, refreshToken, userDTO);
    }

    public AuthResponseDTO register(RegisterRequestDTO request){

        if(request.getEmail() == null || request.getEmail().isBlank()){
            throw new GlobalExceptionHandler.InvalidCredentialsException("Email inválido");
        }

        if(request.getPassword() == null || request.getPassword().length() < 6){
            throw new GlobalExceptionHandler.InvalidCredentialsException("Senha deve ter pelo menos 6 caracteres");
        }

        if(userRepository.findByName(request.getName()).isPresent()){
            throw new GlobalExceptionHandler.UserAlreadyExistsException("Nome de usuário já existente");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user.getId().toString());
        String refreshToken = jwtService.generateRefreshToken(user.getId().toString());
        UserResponseDTO userDTO = UserMapper.toDTO(user);

        return new AuthResponseDTO(accessToken, refreshToken, userDTO);
    }

    public AuthResponseDTO refreshToken(String refreshToken) {
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new GlobalExceptionHandler.InvalidCredentialsException("Refresh Token expirado");
        }

        String userId = jwtService.extractSubject(refreshToken);
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("Usuário não encontrado"));

        String newAccessToken = jwtService.generateAccessToken(user.getId().toString());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId().toString());
        UserResponseDTO userDTO = UserMapper.toDTO(user);

        return new AuthResponseDTO(newAccessToken, newRefreshToken, userDTO);
    }
}
