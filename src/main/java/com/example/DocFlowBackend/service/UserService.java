package com.example.DocFlowBackend.service;

import com.example.DocFlowBackend.dto.AuthResponseDTO;
import com.example.DocFlowBackend.dto.UpdateUserRequestDTO;
import com.example.DocFlowBackend.dto.UserResponseDTO;
import com.example.DocFlowBackend.entity.User;
import com.example.DocFlowBackend.exception.GlobalExceptionHandler;
import com.example.DocFlowBackend.mapper.UserMapper;
import com.example.DocFlowBackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public List<User> listUsers(){
        return userRepository.findAll();
    }

    public UserResponseDTO getById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("Usuário não encontrado"));

        return UserMapper.toDTO(user);
    }

    public Long countTotalUsers (){ return userRepository.count(); }

    public UserResponseDTO deleteUser(String userId){
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("Usuário não encontrado"));

        userRepository.delete(user);

        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public UserResponseDTO updateUser(String userId, UpdateUserRequestDTO dto) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException("Usuário não encontrado"));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }

        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }

        userRepository.save(user);
        return UserMapper.toDTO(user);
    }
}
